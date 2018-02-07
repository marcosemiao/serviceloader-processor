/*
 * Copyright 2015 Marco Semiao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package fr.ms.annotation.processing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import fr.ms.lang.model.ClassElementVisitor;
import fr.ms.util.ServiceLoader;

/**
 *
 * @see <a href="http://marcosemiao4j.wordpress.com">Marco4J</a>
 *
 *
 * @author Marco Semiao
 *
 */
@SupportedAnnotationTypes("fr.ms.util.ServiceLoader")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ServiceLoaderProcessor extends AbstractProcessor {

	private static final String PREFIX = "META-INF/services/";

	private final Map<String, List<String>> services = new HashMap<String, List<String>>();

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ServiceLoader.class);

		if (elements == null || elements.isEmpty()) {
			return false;
		}

		final Messager messager = processingEnv.getMessager();

		boolean error = false;
		for (final Element element : elements) {

			final String impl = element.toString();
			final ClassElementVisitor classElement = classElement(element);
			final Set<String> findInterfaces = classElement.getInterfaces();
			final Set<String> abstractClasses = classElement.getAbstractClasses();
			final Set<String> findInterfacesAndAbstractClasses = new HashSet<String>(findInterfaces);
			findInterfacesAndAbstractClasses.addAll(abstractClasses);

			final Set<String> annotationValues = getAnnotationValues(element);

			if (findInterfacesAndAbstractClasses != null && !findInterfacesAndAbstractClasses.isEmpty()) {
				if (annotationValues == null || annotationValues.isEmpty()) {
					if (findInterfacesAndAbstractClasses.size() == 1) {
						addServices(findInterfacesAndAbstractClasses.iterator().next(), impl);
					} else {
						messager.printMessage(Kind.ERROR, impl
								+ " implements many interfaces or abstract classes, please define the interface or abstract class in the ServiceProvider annotation");
						error = true;
					}
				} else {
					for (final String annotationValue : annotationValues) {
						if (findInterfacesAndAbstractClasses.contains(annotationValue)) {
							addServices(annotationValue, impl);
						} else {
							messager.printMessage(Kind.ERROR, impl + " does not implement " + annotationValue
									+ " interface or abstract class defined in the ServiceProvider annotation");
							error = true;
						}
					}
				}
			}
		}

		if (error) {
			return true;
		}

		final Filer filer = processingEnv.getFiler();

		if (!services.isEmpty()) {
			messager.printMessage(Kind.NOTE, "Service Provider detected");
			for (final Map.Entry<String, List<String>> service : services.entrySet()) {
				messager.printMessage(Kind.NOTE, "************************************");
				final String serviceFile = service.getKey();
				messager.printMessage(Kind.NOTE, "Interface/AbstractClass : " + serviceFile);
				try {
					final FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, "", PREFIX + serviceFile);

					Writer openWriter = null;
					BufferedWriter bw = null;
					try {
						openWriter = fo.openWriter();
						bw = new BufferedWriter(openWriter);
						final List<String> interfacesDeclare = service.getValue();
						for (final String interfaceDeclare : interfacesDeclare) {
							messager.printMessage(Kind.NOTE, "                -> " + interfaceDeclare);
							bw.write(interfaceDeclare);
							bw.newLine();
						}
					} finally {
						if (bw != null) {
							bw.close();
						}
						if (openWriter != null) {
							openWriter.close();
						}
					}

				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return true;
	}

	private void addServices(final String service, final String implementation) {
		List<String> list = services.get(service);

		if (list == null) {
			list = new ArrayList<String>();
			services.put(service, list);
		}

		list.add(implementation);
	}

	private ClassElementVisitor classElement(final Element element) {

		final ClassElementVisitor elementVisitor = new ClassElementVisitor(processingEnv);
		element.accept(elementVisitor, null);

		return elementVisitor;
	}

	private Set<String> getAnnotationValues(final Element element) {
		final Set<String> values = new HashSet<String>();

		final ServiceLoader annotation = element.getAnnotation(ServiceLoader.class);

		try {
			annotation.value();
		} catch (final MirroredTypesException mte) {
			final List<? extends TypeMirror> typeMirrors = mte.getTypeMirrors();
			for (TypeMirror typeMirror : typeMirrors) {
				typeMirror = processingEnv.getTypeUtils().erasure(typeMirror);
				final String inter = typeMirror.toString();
				if (!"java.lang.Object".equals(inter)) {
					values.add(inter);
				}
			}
		}

		return values;
	}
}
