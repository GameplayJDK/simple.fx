/*
 * MIT License
 *
 * Copyright (c) 2017  GameplayJDK
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.gameplayjdk.simplefx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by GameplayJDK on 20.05.2017.
 */
public abstract class Controller<V extends Parent> {

    private boolean ready;

    private V view;

    public Controller() {
        this(false, null);
    }

    public Controller(String name) {
        this(false, name);
    }

    private Controller(boolean fromFxmlLoader, String name) {
        this.ready = fromFxmlLoader;

        if (fromFxmlLoader) {
            return;
        }

        this.view = this.getView(name);
    }

    @FXML
    protected abstract void initialize();

    private V getView(String name) {
        if (((name == null) ? "" : name).isEmpty()) {
            name = this.getClass().getSimpleName();
            name = name.substring(0, name.length() - "Controller".length());
        }

        name = name + ".fxml";

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource(name));
            fxmlLoader.setControllerFactory(new Callback<Class<?>, Object>() {
                @Override
                public Object call(Class<?> parameter) {
                    String currentControllerName = Controller.this.getClass().getName();
                    String requiredControllerName = parameter.getName();

                    if (currentControllerName.equals(requiredControllerName)) {
                        if (Controller.this.view == null) {
                            return Controller.this;
                        }
                    }

                    Constructor<Controller<V>> controllerConstructor;
                    Object[] controllerConstructorArgs;

                    System.err.println("WARNING: " + ("Factory of " + currentControllerName + " is loading a " + requiredControllerName + " controller"));

                    try {
                        if (Controller.class.isAssignableFrom(parameter)) {
                            controllerConstructor = (Constructor<Controller<V>>) parameter.getDeclaredConstructor(boolean.class, String.class);
                            controllerConstructorArgs = new Object[] {
                                    true, null
                            };
                        } else {
                            controllerConstructor = (Constructor<Controller<V>>) parameter.getDeclaredConstructor();
                            controllerConstructorArgs = new Object[] {
                            };
                        }

                        try {
                            return controllerConstructor.newInstance(controllerConstructorArgs);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                            ex.printStackTrace();
                        }
                    } catch (NoSuchMethodException ex) {
                        ex.printStackTrace();
                    }

                    return null;
                }
            });

            return fxmlLoader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public boolean isReady() {
        return this.ready;
    }

    protected void setReady() {
        if (this.ready) {
            return;
        }

        this.ready = true;
        this.initialize();
    }

    public V getView() {
        if (this.view == null) {
            throw new NullPointerException("View is null. Please set it manually in your " + this.getClass().getSimpleName() + " controller");
        }

        return this.view;
    }

    public void setView(V view) {
        this.view = view;
    }
}
