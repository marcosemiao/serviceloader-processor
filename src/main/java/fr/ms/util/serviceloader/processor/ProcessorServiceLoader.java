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
package fr.ms.util.serviceloader.processor;

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
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import fr.ms.model.ClassElementVisitor;
import fr.ms.util.ServiceProvider;

/**
 *
 * @see <a href="http://marcosemiao4j.wordpress.com">Marco4J</a>
 *
 *
 * @author Marco Semiao
 *
 */
@SupportedAnnotationTypes("fr.ms.util.ServiceProvider")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ProcessorServiceLoader extends AbstractProcessor {

    private static final String PREFIX = "META-INF/services/";

    private final Map<String, List<String>> services = new HashMap<String, List<String>>();

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
	final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ServiceProvider.class);

	if (elements == null || elements.isEmpty()) {
	    return false;
	}

	for (final Element element : elements) {

	    final String impl = element.toString();
	    final Set<String> findInterfaces = findInterfaces(element);

	    final Set<String> annotationValues = getAnnotationValues(element);

	    if (findInterfaces != null && !findInterfaces.isEmpty()) {
		if (annotationValues == null || annotationValues.isEmpty()) {
		    if (findInterfaces.size() == 1) {
			addInterface(findInterfaces.iterator().next(), impl);
		    } else {
			throw new ServiceLoaderAnnotationException(impl + "implémente plusieurs interfaces, vous devez les définir vous même dans l'annotation");
		    }
		} else {
		    for (final String annotationValue : annotationValues) {
			if (findInterfaces.contains(annotationValue)) {
			    addInterface(annotationValue, impl);
			} else {
			    throw new ServiceLoaderAnnotationException(impl + " implémente pas l'interface " + annotationValue + " définie dans l'annotation");
			}
		    }
		}
	    }
	}

	final Filer filer = processingEnv.getFiler();

	for (final Map.Entry<String, List<String>> service : services.entrySet()) {
	    final String serviceFile = service.getKey();
	    try {
		final FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, "", PREFIX + serviceFile);

		Writer openWriter = null;
		BufferedWriter bw = null;
		try {
		    openWriter = fo.openWriter();
		    bw = new BufferedWriter(openWriter);
		    final List<String> interfacesDeclare = service.getValue();
		    for (final String interfaceDeclare : interfacesDeclare) {
			bw.append(interfaceDeclare);
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

	return true;
    }

    private void addInterface(final String service, final String implementation) {
	List<String> list = services.get(service);

	if (list == null) {
	    list = new ArrayList<String>();
	    services.put(service, list);
	}

	list.add(implementation);
    }

    private Set<String> findInterfaces(final Element element) {

	final ClassElementVisitor elementVisitor = new ClassElementVisitor();
	element.accept(elementVisitor, null);

	final Set<String> interfaces = elementVisitor.getInterfaces();

	return interfaces;
    }

    private Set<String> getAnnotationValues(final Element element) {
	final Set<String> values = new HashSet<String>();

	final ServiceProvider annotation = element.getAnnotation(ServiceProvider.class);

	try {
	    annotation.value();
	} catch (final MirroredTypesException mte) {
	    final List<? extends TypeMirror> typeMirrors = mte.getTypeMirrors();
	    for (final TypeMirror typeMirror : typeMirrors) {
		final String inter = typeMirror.toString();
		if (!"java.lang.Object".equals(inter)) {
		    values.add(inter);
		}
	    }
	}

	return values;
    }
}
