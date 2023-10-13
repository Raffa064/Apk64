# Transpiltion steps

This is a example of the JS64 tranpiler working tree.

Now, we will see step by step what happens during the transpilation process, starting by the following code in JS64:

```javascript
export Class::value = STRING

Class::method() {
    # comment
    $attr = $value
}
```

## 1) transpile_classDefinition
Automatically create "class" definitions:

```javascript
function Class() {
    %Class%
}
 
export Class::value = STRING

Class::method() {
    # comment
    $attr = $value
}
```

## 2) transpile_exportSintax

Trnspiles the export statment

```javascript
function Class() {
    %Class%
}
 
Class.prototype.value = "STRING"

Class::method() {
    # comment
    $attr = $value
}
```

## 3) transpile_defaultExportValues

Apply default values to the exported props:

```javascript
function Class() {
    this.value = ''
}
 
Class.prototype.value = "STRING"

Class::method() {
    # comment
    $attr = $value
}
```
 
## 4) transpile_methodSintax

Transpile the methods into the common JS sintax:

```javascript
function Class() {
    this.value = ''
}
 
Class.prototype.value = "STRING"

Class.prototype.method = function() {
    # comment
    $attr = $value
}
```
 
## 5) transpile_referenceSintax

Convert "$prop" into "this.prop":

```javascript 
function Class() {
    this.value = ''
}
 
Class.prototype.value = "STRING"

Class.prototype.method = function() {
    # comment
    this.attr = this.value
}
```

## 6) transpile_sharpCommentsSintax

Convert "#" inline comment to "//":

```javascript
function Class() {
    this.value = ''
}
 
Class.prototype.value = "STRING"

Class.prototype.method = function() {
// comment
    this.attr = this.value
}
```
