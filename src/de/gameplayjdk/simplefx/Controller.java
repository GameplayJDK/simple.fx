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
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by GameplayJDK on 20.05.2017.
 */
public abstract class Controller<V extends Parent> {

    private boolean ready;

    private static Locale locale = Locale.ROOT;

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

    protected abstract void initialize();

    private String getSimpleName(String name) {
        if (name == null) {
            name = "";
        }

        name = name.trim();

        if (name.isEmpty()) {
            name = this.getClass().getSimpleName();
        }

        if (name.endsWith("Controller")) {
            name = name.substring(0, (name.length() - "Controller".length()));
        }

        return name;
    }

    private String getCanonicalName(String name) {
        if (name == null) {
            name = "";
        }

        name = name.trim();

        if (name.isEmpty()) {
            name = this.getSimpleName(name);
        }

        String canonicalName = this.getClass().getCanonicalName();
        canonicalName = canonicalName.substring(0, (canonicalName.length() - this.getClass().getSimpleName().length()));
        canonicalName = canonicalName + name;

        return canonicalName;
    }

    private V getView(String name) {
        name = this.getSimpleName(name);

        String fxmlResource = name + ".fxml";

        String bundleName = this.getCanonicalName(name);
        bundleName = bundleName + "Bundle";

        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, Controller.locale);

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource(fxmlResource));
            fxmlLoader.setResources(resourceBundle);
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
        } catch (IOException | NullPointerException ex) {
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

    public static Locale getLocale() {
        return Controller.locale;
    }

    public static void setLocale(Locale locale) {
        Controller.locale = locale;
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
