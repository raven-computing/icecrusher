/* 
 * Copyright (C) 2021 Raven Computing
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

import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Represents a proxy for a concrete {@link Controller} instance inside the 
 * application's activity stack. This interface provides methods that can be
 * used to call a method of another Controller. Each call request will be 
 * redirected to the actual Controller instance in memory this proxy represents.
 * Therefore each method invocation will be forwarded to the underlying Controller
 * instance.
 * 
 * <p>The concrete Controller in question must explicitly allow a remote invokation
 * of any particular method by another controller. Such methods must be exposed and 
 * marked as such by the underlying Controller of this proxy.
 *
 */
public interface ProxyController {

    /**
     * Invokes the exposed public member method with the specified key. The proxy will
     * dispatch the method call to the underlying Controller it is responsible for.<br>
     * If the annotation of the marked method does not specify an invocation key, then
     * the identifying name will be used to locate the desired method
     * 
     * @param key The invocation key of the method to call by proxy
     * @return The return value of method to be invoked. If the underlying method has a 
     *         return type of <code>void</code>, then the returned Object is null
     * @throws Exception At any point if at least one of the following conditions are met:
     *                   <li>No exposed public method was found for the underlying 
     *                   Controller class</li>
     *                   <li>At the time of this call no instance of the underlying 
     *                   Controller class is found on the internal activity stack, either 
     *                   because it was removed by the application or it never existed in 
     *                   the first place</li>
     *                   <li>The underlying method itself throws an exception. This gets 
     *                   wrapped in an InvocationTargetException</li>
     */
    public Object call(String key) throws Exception;

    /**
     * Invokes the exposed public member method with the specified key. The proxy will
     * dispatch the method call to the underlying Controller it is responsible for.<br>
     * If the annotation of the marked method does not specify an invocation key, then
     * the identifying name will be used to locate the desired method
     * 
     * @param key The invocation key of the method to call by proxy
     * @param args The arguments to pass to the underlying method to be invoked. May be an 
     *             empty array if the method takes no parameters
     * @return The return value of method to be invoked. If the underlying method has a 
     *         return type of <code>void</code>, then the returned Object is null
     * @throws Exception At any point if at least one of the following conditions are met:
     *                   <li>No exposed public method was found for the underlying 
     *                   Controller class</li>
     *                   <li>At the time of this call no instance of the underlying 
     *                   Controller class is found on the internal activity stack, either 
     *                   because it was removed by the application or it never existed in 
     *                   the first place</li>
     *                   <li>The underlying method itself throws an exception. This gets 
     *                   wrapped in an InvocationTargetException</li>
     */
    public Object call(String key, Object... args) throws Exception;

    /**
     * Gets the main stage the Controller of this proxy is part of
     * 
     * @return A reference to the main <code>Stage</code> of this application
     * @throws ControllerNotFoundException If at the time of this call no instance of the 
     *                                     underlying Controller class is found on the 
     *                                     internal activity stack, either because it was 
     *                                     removed by the application or the activity of 
     *                                     the Controller has finsihed
     */
    public Stage getStage() throws ControllerNotFoundException;

    /**
     * Gets the root node of the scene graph of this application
     * 
     * @return A reference to the root <code>Pane</code> of this application
     * @throws ControllerNotFoundException If at the time of this call no instance of the 
     *                                     underlying Controller class is found on the 
     *                                     internal activity stack, either because it was 
     *                                     removed by the application or the activity of 
     *                                     the Controller has finsihed
     */
    public Pane getRootNode() throws ControllerNotFoundException;

    /**
     * Gets the local root node of the scene graph of this proxy's Controller's activity
     * 
     * @return A reference to the root <code>Parent</code> of the activity this proxy's 
     *         Controller is responsible for
     * @throws ControllerNotFoundException If at the time of this call no instance of the 
     *                                     underlying Controller class is found on the 
     *                                     internal activity stack, either because it was 
     *                                     removed by the application or the activity of 
     *                                     the Controller has finsihed
     */
    public Parent getLocalRootNode() throws ControllerNotFoundException;

}
