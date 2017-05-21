# simple.fx

Simple JavaFx library to deal with controllers.

The idea behind this library is to allow the controller to be instantiated in java code while loading the associated fxml file and to reference it in the `fx:controller` attribute in that file.

## What's in it?

The library only contains two classes which makes its size only about `4kb`.

 - One of them, `Controller`, is the most basic implementation of a simple.fx JavaFx controller.
 - The other one, `BaseController`, is nearly the same but requires an additional main class to be specified.

## How to install it?

For that just download the latest release and add the jar to your project as a library.

## How to use it?

To use the library simply extend one of the two above classes based on your requirements:

```java
// ...

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import de.gameplayjdk.simplefx.Controller;

// Associated with "My.fxml" in the same package by default
public class MyController extends Controller<AnchorPane> {
    
    public MyController() {
        super();
    }
    
    @FXML
    @Override
    protected void initialize() {
        // ...
    }
    
    // ...
}

// ...
```

or

```java
// ...

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import de.gameplayjdk.simplefx.BaseController;

// Associated with "MyBase.fxml" in the same package by default
public class MyBaseController extends BaseController<AnchorPane, MyMain> {
    
    public MyBaseController(MyMain main) {
        super(main);
    }
    
    @FXML
    @Override
    protected void initialize() {
        // ...
    }
    
    // ...
}

// ...
```

An example of an application main class would be something like this:

```java
// ...

import javafx.scene.Scene;
import javafx.application.Application;

public class MyMain extends Application {
    
    private static MyMain instance;
    
    // ...
    
    public Main() {
        super();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MyController controller = new MyController();
        Scene scene = new Scene(controller.getView());
        
        // or
        MyBaseController baseController = new MyBaseController(this);
        scene = new Scene(baseController.getView());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        Platform.exit();
    }
    
    // ...
    
    public static void main(String[] args) {
        Application.launch(MyMain.class, args);
    }
    
    public static MyMain getInstance() {
        if (MyMain.instance == null) {
            MyMain.instance = new MyMain();
        }
        
        return MyMain.instance;
    }
}

```

### Some more details please!

Ok, so there is some hidden magic with the name of the fxml file.

While you can set it when calling the super constructor you can be lazy and leave the name empty, like `""`, or leave it null which will default the name to the controller class name minus the `Controller` part. E.g. a controller named `xController` will try to load the file `x.fxml` if not being told another name. But in any case be sure to leave out the `.fxml` extension.

## Is there anything else important?

**Notice that it's required to start loading the layout via `new MyController()`! If not, the default `FXMLLoader` will be used which will result in an `javafx.fxml.LoadException` stating `Controller value already specified.`!**

You should also make sure that in controllers that are only referenced in fxml set their view manually in their `initialize()` method using `setView(view)`. Otherwise a NPE will be thrown when you try to access the view using `getView()`.

**Also be aware of the fact, that due to JavaFx internal processing the `initialize()` method will be called BEFORE the constructor is completely executed!**
The problem here is that, if you plan to set class variables in the constructor, you will only be able to access them AFTER `initialize()` has been called. Therefor it will be called TWICE, once before and once after the class is ready. You can use `isReady()` to determine whether that's the case.

## How does it work?

The library uses an enhanced controller factory for the `FXMLLoader` that distinguishes between the simple.fx JavaFx controller that triggered the loading, sub-classes of the simple.fx JavaFx controller and other controllers.
That enables it to use a different instantiation technique for each of them.
