---
hide:
- toc
---

# Thymeleaf cheatsheet

[Thymeleaf](https://www.thymeleaf.org) is a nice HTML templating engine designed to ensure that templates are always viewable without being
processed, which is convenient for rapid iteration and prototyping. It's used to generate the download page in the site task. This page is
not meant to be a comprehensive tutorial in  Thymeleaf syntax. It is designed to give you the basics in 60 seconds.

[Read the official documentation](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html){ .md-button .md-button--primary }

Expressions are written in [OGNL](https://commons.apache.org/proper/commons-ognl/language-guide.html) and can navigate an object graph.

```html
<html xmlns:th="https://www.thymeleaf.org" lang="en">

Setting tag bodies:

<p th:text="${object.property.anotherProperty}"></p>
<p>Substitutions inside [[${object.property.anotherProperty}]] text</p>

Substitutions in scripts:

<script th:inline="javascript">
    var foo = "dummy" /*[[${object.property}]]*/;
</script>

Setting known attributes with short syntax, and unknown with long form:

<div th:class="${thing}"></div>
<div th:attr="data-foo=${thing},other-unknown-attr=${otherThing}"></div>

Look up localized messages:

<p th:text="#{message}"></p>

Object 'with' operator:

<div th:object="${someObject}">
    <ul>
        <li>Foo property: [[*{foo}]]</li>
        <li>Bar property: [[*{bar}]]</li>
    </ul>
</div>

Iterate (note that the 'each' is not defined on the container element):

<ul>
    <li th:each="item : ${someList}">Item [[${item.thing}]]</li>
</ul>

Define a no-op block that is unwrapped at render time:

<ul>
    <th:block th:each="item : ${someList}">
        <li>Item [[${item.thing}]]</li>
    </th:block>
</ul>

Include another file as children of a tag (the basename is without an extension). 

<div th:include="file-basename"></div>

</html>
```
