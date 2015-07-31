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
package fr.ms.model;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 *
 * @see <a href="http://marcosemiao4j.wordpress.com">Marco4J</a>
 *
 *
 * @author Marco Semiao
 *
 */
public class ClassTypeVisitor extends SimpleTypeVisitor6<Void, Void> {

    private final ClassElementVisitor elementVisitor = new ClassElementVisitor();

    @Override
    public Void visitDeclared(final DeclaredType declaredType, final Void type) {
	visitDeclared(declaredType);

	return defaultAction(declaredType, type);
    }

    private void visitDeclared(final DeclaredType declaredType) {
	if (declaredType != null) {
	    final Element element = declaredType.asElement();
	    if (element != null) {
		element.accept(elementVisitor, null);
	    }
	}
    }

    public Set<String> getInterfaces() {
	return elementVisitor.getInterfaces();
    }
}
