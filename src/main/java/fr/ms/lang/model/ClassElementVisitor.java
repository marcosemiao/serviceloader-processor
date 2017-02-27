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
package fr.ms.lang.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor6;

/**
 *
 * @see <a href="http://marcosemiao4j.wordpress.com">Marco4J</a>
 *
 *
 * @author Marco Semiao
 *
 */
public class ClassElementVisitor extends SimpleElementVisitor6<Void, Void> {

	private final ProcessingEnvironment processingEnv;

	private final Set<String> interfaces = new HashSet<String>();

	private final Set<String> superClasses = new HashSet<String>();

	public ClassElementVisitor(final ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}

	@Override
	public Void visitType(final TypeElement typeElement, final Void type) {
		visitType(typeElement);

		return super.visitType(typeElement, type);
	}

	private void visitType(final TypeElement typeElement) {
		final List<? extends TypeMirror> interfacesType = typeElement.getInterfaces();

		if (interfacesType != null && !interfacesType.isEmpty()) {
			for (TypeMirror typeMirror : interfacesType) {
				typeMirror = processingEnv.getTypeUtils().erasure(typeMirror);

				interfaces.add(typeMirror.toString());
			}
		}

		final TypeMirror superClassType = typeElement.getSuperclass();

		if (superClassType != null && !"java.lang.Object".equals(superClassType.toString())) {
			superClasses.add(superClassType.toString());
			final ClassTypeVisitor typeVisitor = new ClassTypeVisitor(processingEnv);
			superClassType.accept(typeVisitor, null);
			final Set<String> superClassInterfaces = typeVisitor.getInterfaces();
			interfaces.addAll(superClassInterfaces);
		}
	}

	public Set<String> getInterfaces() {
		return Collections.unmodifiableSet(interfaces);
	}

	public Set<String> getSuperClasses() {
		return Collections.unmodifiableSet(superClasses);
	}
}
