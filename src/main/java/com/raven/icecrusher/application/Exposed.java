/* 
 * Copyright (C) 2020 Raven Computing
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.raven.icecrusher.application;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation that can be attached to method declarations of {@link Controller} 
 * types in order to mark them as invocable from the outside by other Controllers.
 * 
 * <p>Methods using this annotation must be declared <i>public</i>. Any other access
 * modifier will cause that method to be unaccessible to other Controllers.
 * 
 * <p>Each method using this annotation will not be directly called by another Controller
 * but by a proxy which will forward any calls to the concrete method.
 * 
 * <p>The one and only attribute of this annotation specifies the key by which the 
 * underlying method can be called. Please note that no validations regarding uniqueness
 * of invocation keys are performed. It is the responsibility of the developer to ensure 
 * uniqueness. If the key is omitted then the identifying name of the underlying method is 
 * used to reference the callable method. For that reason you should make sure to only use
 * Strings as keys that cannot be confused with identifiers of method signatures.
 *
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Exposed {

    String value() default "";

}
