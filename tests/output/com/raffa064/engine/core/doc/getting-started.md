# Getting Started

For create a new game with Square Engine, start by making a (App)[app.md] instance:

```java
App app = new App();
app.init();
app.loadProject(Gdx.files.internal("project")); // Load files from assets/project folder
```

After it, make a Scene object, set as App scene, and place objects into it:

```java
Scene scene = new Scene();
app.setScene(scene);

GameObject emptyObj = new GameObject();
scene.addChild(emptyObj);
```

The above code example is creating a *empty* object, but it has no utility, because it haven't any defined behavior.

To define bahaviors into a [Game0bject](game-object.md), you can add into them some (native components)[./components/native-components.md], like [Transform2D](./components/transform2d.md) and [Image](./components/image.md), or a custom [script component](./components/script-components.md), that uses (JS64)[./js64.md] as scripting lenguage.

```
GameObject bg = new GameObject();

Transform2D transform = (Transform2D) app.Component.create("Transform2D");
transform.pos.x = app.viewportWidth / 2;
transform.pos.y = app.viewportHeight / 2;
bg.add(transform);

Image img = (Image) app.Component.create("Image");
img.texturePath = "project/bg.jpg";
bg.add(img);

scene.addChild(bg);
```

The last code, will create a background image, with a Transform2D setting the position to the center os the screen, and with an Image that is located in "project/bg.jpg".

For example porposes, I'll show you a simple JS64 component to make it background moving. The only necessary thing to use script components is to create a ".js" file inside the project folder, defined by the loadProject method, after it, the script files will be automatically compiled by [ScriptEngine](script-engine.md):

```javascript
Move::ready() {
	# Function called when object is addedd to scene
	$transform = $obj.get('Transform2D')
}

Move::process(delta) {
	# Function called every single frame
	$transform.pos.x += 5
}

Move::exit() {
	# Function called when object is removed from the scene
}
```

Considering that this file is placed in the project folder, like I have already explained, you will only need to add it into the *bg* object:

```java
GameObject bg = new GameObject();

/* Other components... */

Script move = app.Component.create('Move');
bg.add(move);
```

> NOTE: the **create** method needs the Component name, not the file name!<br>You can add more than one component per file if you need, but it's not recommended.

Like you can see in the last example, the moving speed is 5 pixels per frame. if you want to make it as a "param", you can export the speed like the following:


```javascript
export Move::speed = FLOAT

Move::ready() {
	# Function called when object is addedd to scene
	$transform = $obj.get('Transform2D')
}

Move::process(delta) {
	# Function called every single frame
	$transform.pos.x += $speed
}

Move::exit() {
	# Function called when object is removed from the scene
}
```

After exported, the **prop** is placed on a export list, and receive a default value. In this case, the speed prop is a FLOAT, and the default value for it is "0.0". To see more about exported props, see the (exported props documentaion)[./script/exported-props.md].

Now, with speed prop exported, you can change it's value in Java with something like:

```java
Script move = app.Component.create('Move');
move.set('speed', 10) // The script props is not directily acessible into Java
```

## Another way

In the last examples, the objects are created using new operator. This isn't wrong, but exists another way, that is especilly created for use inside script, but you can use in Java too.

For create objects, you can use the (SceneAPI)[./api/scene.md], that is stored in "app.Scene":

```
SceneAPI Scene = app.Scene(); // If you are inside a Component or script it will be automatically declarated.

GameObject obj = Scene.createObject("ObjectName", component1, component2, component3...);

Scene.addToScene(obj);
```

Like is saiyed in the comment, the SceneAPI is auto declarated in compoent scope, it means that you can refer to it in any component, including Native components and Script components.


