# HOCON specification

HOCON (`application/hocon`) is the Human Optimized Config Object Notation. It's a superset of JSON that's used to configure Conveyor. This document is a simplified and cleaned up version of the spec. [The canonical version of the spec can be found here](https://raw.githubusercontent.com/lightbend/config/master/HOCON.md).

## Goals / Background

The primary goal is: keep the semantics (tree structure; set of
types; encoding/escaping) from JSON, but make it more convenient
as a human-editable config file format.

The following features are desirable, to support human usage:

 - less noisy / less pedantic syntax
 - ability to refer to another part of the configuration (set a value to
   another value)
 - import/include another configuration file into the current file
 - a mapping to a flat properties list such as Java's system properties
 - ability to get values from environment variables
 - ability to write comments

Implementation-wise, the format should have these properties:

 - a JSON superset, that is, all valid JSON should be valid and
   should result in the same in-memory data that a JSON parser
   would have produced.
 - be deterministic; the format is flexible, but it is not
   heuristic. It should be clear what's invalid and invalid files
   should generate errors.
 - require minimal look-ahead; should be able to tokenize the file
   by looking at only the next three characters. (right now, the
   only reason to look at three is to find "//" comments;
   otherwise you can parse looking at two.)

HOCON is significantly harder to specify and to parse than
JSON. Think of it as moving the work from the person maintaining
the config file to the computer program.

## Definitions

 - a _key_ is a string JSON would have to the left of `:` and a _value_ is
   anything JSON would have to the right of `:`. i.e. the two
   halves of an object _field_.

 - a _value_ is any "value" as defined in the JSON spec, plus
   unquoted strings and substitutions as defined in this spec.

 - a _simple value_ is any value excluding an object or array
   value.

 - a _field_ is a key, any separator such as ':', and a value.

 - references to a _file_ ("the file being parsed") can be
   understood to mean any byte stream being parsed, not just
   literal files in a filesystem.


## Unchanged from JSON

Much of this is defined with reference to JSON; you can find the
JSON spec at https://json.org/ of course.

 - files must be valid UTF-8
 - quoted strings are in the same format as JSON strings
 - values have possible types: string, number, object, array, boolean, null
 - allowed number formats matches JSON; as in JSON, some possible
   floating-point values are not represented, such as `NaN`

## Comments

Anything between `//` or `#` and the next newline is considered a comment
and ignored, unless the `//` or `#` is inside a quoted string.

## Omit root braces

JSON documents must have an array or object at the root. Empty
files are invalid documents, as are files containing only a
non-array non-object value such as a string. In HOCON, if the file does not begin with a square bracket or
curly brace, it is parsed as if it were enclosed with `{}` curly
braces. A HOCON file is invalid if it omits the opening `{` but still has
a closing `}`; the curly braces must be balanced.

## Key-value separator

The `=` character can be used anywhere JSON allows `:`, i.e. to
separate keys from values. If a key is followed by `{`, the `:` or `=` may be omitted. So
`"foo" {}` means `"foo" : {}`

## Commas

Values in arrays, and fields in objects, need not have a comma
between them as long as they have at least one ASCII newline
(`\n`, decimal value 10) between them.

The last element in an array or last field in an object may be
followed by a single comma. This extra comma is ignored.

 - `[1,2,3,]` and `[1,2,3]` are the same array.
 - `[1\n2\n3]` and `[1,2,3]` are the same array.
 - `[1,2,3,,]` is invalid because it has two trailing commas.
 - `[,1,2,3]` is invalid because it has an initial comma.
 - `[1,,2,3]` is invalid because it has two commas in a row.
 - these same comma rules apply to fields in objects.

## Whitespace

The JSON spec simply says "whitespace"; in HOCON whitespace is
defined as follows:

 - any Unicode space separator (Zs category), line separator (Zl
   category), or paragraph separator (Zp category), including
   nonbreaking spaces (such as 0x00A0, 0x2007, and 0x202F).
   The BOM (0xFEFF) must also be treated as whitespace.
 - tab (`\t` 0x0009), newline ('\n' 0x000A), vertical tab ('\v'
   0x000B)`, form feed (`\f' 0x000C), carriage return ('\r'
   0x000D), file separator (0x001C), group separator (0x001D),
   record separator (0x001E), unit separator (0x001F).

In Java, the `isWhitespace()` method covers these characters with
the exception of nonbreaking spaces and the BOM. While all Unicode separators should be treated as whitespace, in
this spec "newline" refers only and specifically to ASCII newline
0x000A.

## Duplicate keys and object merging

The JSON spec does not clarify how duplicate keys in the same
object should be handled. In HOCON, duplicate keys that appear
later override those that appear earlier, unless both values are
objects. If both values are objects, then the objects are merged.

!!! note
    This would make HOCON a non-superset of JSON if you assume
    that JSON requires duplicate keys to have a behavior. The
    assumption here is that duplicate keys are invalid JSON.

To merge objects:

 - add fields present in only one of the two objects to the merged
   object.
 - for non-object-valued fields present in both objects,
   the field found in the second object must be used.
 - for object-valued fields present in both objects, the
   object values should be recursively merged according to
   these same rules.

Object merge can be prevented by setting the key to another value
first. This is because merging is always done two values at a
time; if you set a key to an object, a non-object, then an object,
first the non-object falls back to the object (non-object always
wins), and then the object falls back to the non-object (no
merging, object is the new value). So the two objects never see
each other.

These two are equivalent:

    {
        "foo" : { "a" : 42 },
        "foo" : { "b" : 43 }
    }
    
    {
        "foo" : { "a" : 42, "b" : 43 }
    }

And these two are equivalent:

    {
        "foo" : { "a" : 42 },
        "foo" : null,
        "foo" : { "b" : 43 }
    }
    
    {
        "foo" : { "b" : 43 }
    }

The intermediate setting of `"foo"` to `null` prevents the object merge.

## Unquoted strings

A sequence of characters outside of a quoted string is a string
value if:

 - it does not contain "forbidden characters": '$', '"', '{', '}',
   '[', ']', ':', '=', ',', '+', '#', '`', '^', '?', '!', '@',
   '*', '&', '\' (backslash), or whitespace.
 - it does not contain the two-character string "//" (which
   starts a comment)
 - its initial characters do not parse as `true`, `false`, `null`,
   or a number.

Unquoted strings are used literally, they do not support any kind
of escaping. Quoted strings may always be used as an alternative
when you need to write a character that is not permitted in an
unquoted string.

`truefoo` parses as the boolean token `true` followed by the
unquoted string `foo`. However, `footrue` parses as the unquoted
string `footrue`. Similarly, `10.0bar` is the number `10.0` then
the unquoted string `bar` but `bar10.0` is the unquoted string
`bar10.0`. (In practice, this distinction doesn't matter much
because of value concatenation; see later section.)

In general, once an unquoted string begins, it continues until a
forbidden character or the two-character string "//" is
encountered. Embedded (non-initial) booleans, nulls, and numbers
are not recognized as such, they are part of the string. An unquoted string may not _begin_ with the digits 0-9 or with a
hyphen (`-`, 0x002D) because those are valid characters to begin a
JSON number. The initial number character, plus any valid-in-JSON
number characters that follow it, must be parsed as a number
value. Again, these characters are not special _inside_ an
unquoted string; they only trigger number parsing if they appear
initially.

Note that quoted JSON strings may not contain control characters
(control characters include some whitespace characters, such as
newline). This rule is from the JSON spec. However, unquoted
strings have no restriction on control characters, other than the
ones listed as "forbidden characters" above. Some of the "forbidden characters" are forbidden because they
already have meaning in JSON or HOCON, others are essentially
reserved keywords to allow future extensions to this spec.

## Multi-line strings

Multi-line strings are similar to Python or Scala, using triple
quotes. If the three-character sequence `"""` appears, then all
Unicode characters until a closing `"""` sequence are used
unmodified to create a string value. Newlines and whitespace
receive no special treatment. Unlike Scala, and unlike JSON quoted
strings, Unicode escapes are not interpreted in triple-quoted
strings.

In Python, `"""foo""""` is a syntax error (a triple-quoted string
followed by a dangling unbalanced quote). In Scala, it is a
four-character string `foo"`. HOCON works like Scala; any sequence
of at least three quotes ends the multi-line string, and any
"extra" quotes are part of the string.

## Value concatenation

The value of an object field or array element may consist of
multiple values which are combined. There are three kinds of value
concatenation:

 - if all the values are simple values (neither objects nor
   arrays), they are concatenated into a string.
 - if all the values are arrays, they are concatenated into
   one array.
 - if all the values are objects, they are merged (as with
   duplicate keys) into one object.

String value concatenation is allowed in field keys, in addition
to field values and array elements. Objects and arrays do not make
sense as field keys.

### String value concatenation

String value concatenation is the trick that makes unquoted
strings work; it also supports substitutions (`${foo}` syntax) in
strings. Only simple values participate in string value
concatenation. Recall that a simple value is any value other than
arrays and objects. As long as simple values are separated only by non-newline
whitespace, the _whitespace between them is preserved_ and the
values, along with the whitespace, are concatenated into a string. String value concatenations never span a newline, or a character
that is not part of a simple value.

A string value concatenation may appear in any place that a string
may appear, including object keys, object values, and array
elements. Whenever a value would appear in JSON, a HOCON parser instead
collects multiple values (including the whitespace between them)
and concatenates those values into a string. Whitespace before the first and after the last simple value must
be discarded. Only whitespace _between_ simple values must be
preserved.

!!! example
    ` foo bar baz ` parses as three unquoted strings,
    and the three are value-concatenated into one string. The inner
    whitespace is kept and the leading and trailing whitespace is
    trimmed. The equivalent string, written in quoted form, would be
    `"foo bar baz"`. Value concatenating `foo bar` (two unquoted strings     with whitespace) and quoted string `"foo bar"` would result in the       same in-memory representation, seven characters.

For purposes of string value concatenation, non-string values are
converted to strings as follows (strings shown as quoted strings):

 - `true` and `false` become the strings `"true"` and `"false"`.
 - `null` becomes the string `"null"`.
 - quoted and unquoted strings are themselves.
 - numbers should be kept as they were originally written in the
   file. For example, if you parse `1e5` then you might render
   it alternatively as `1E5` with capital `E`, or just `100000`.
   For purposes of value concatenation, it should be rendered
   as it was written in the file.
 - a substitution is replaced with its value which is then
   converted to a string as above.
 - it is invalid for arrays or objects to appear in a string value
   concatenation.

A single value is never converted to a string. That is, it would
be wrong to value concatenate `true` by itself; that should be
parsed as a boolean-typed value. Only `true foo` (`true` with
another simple value on the same line) should be parsed as a value
concatenation and converted to a string.

### Array and object concatenation

Arrays can be concatenated with arrays, and objects with objects,
but it is an error if they are mixed. For purposes of concatenation, "array" also means "substitution that resolves to an array" and "object" also means "substitution that resolves to an object."

Within an field value or array element, if only non-newline
whitespace separates the end of a first array or object or
substitution from the start of a second array or object or
substitution, the two values are concatenated. Newlines may occur
_within_ the array or object, but not _between_ them. Newlines
_between_ prevent concatenation.

For objects, "concatenation" means "merging", so the second object
overrides the first. Arrays and objects cannot be field keys, whether concatenation is involved or not.

Here are several ways to define `a` to the same object value:

    // one object
    a : { b : 1, c : 2 }
    // two objects that are merged via concatenation rules
    a : { b : 1 } { c : 2 }
    // two fields that are merged
    a : { b : 1 }
    a : { c : 2 }

Here are several ways to define `a` to the same array value:

    // one array
    a : [ 1, 2, 3, 4 ]
    // two arrays that are concatenated
    a : [ 1, 2 ] [ 3, 4 ]
    // a later definition referring to an earlier
    // (see "self-referential substitutions" below)
    a : [ 1, 2 ]
    a : ${a} [ 3, 4 ]

A common use of object concatenation is "inheritance":

    data-center-generic = { cluster-size = 6 }
    data-center-east = ${data-center-generic} { name = "east" }

A common use of array concatenation is to add to paths:

    path = [ /bin ]
    path = ${path} [ /usr/bin ]

!!! note Concatenation with whitespace and substitutions

    When concatenating substitutions such as `${foo} ${bar}`, the
    substitutions may turn out to be strings (which makes the
    whitespace between them significant) or may turn out to be objects
    or lists (which makes it irrelevant). Unquoted whitespace must be
    ignored in between substitutions which resolve to objects or
    lists. Quoted whitespace should be an error.

### Arrays without commas or newlines

Arrays allow you to use newlines instead of commas, but not
whitespace instead of commas. Non-newline whitespace will produce
concatenation rather than separate elements.

    // this is an array with one element, the string "1 2 3 4"
    [ 1 2 3 4 ]
    // this is an array of four integers
    [ 1
      2
      3
      4 ]
    
    // an array of one element, the array [ 1, 2, 3, 4 ]
    [ [ 1, 2 ] [ 3, 4 ] ]
    // an array of two arrays
    [ [ 1, 2 ]
      [ 3, 4 ] ]

If this gets confusing, just use commas. The concatenation
behavior is useful rather than surprising in cases like:

    [ This is an unquoted string my name is ${name}, Hello ${world} ]
    [ ${a} ${b}, ${x} ${y} ]

Non-newline whitespace is never an element or field separator.

## Path expressions

Path expressions are used to write out a path through the object
graph. They appear in two places; in substitutions, like
`${foo.bar}`, and as the keys in objects like `{ foo.bar : 42 }`.

Path expressions are syntactically identical to a value
concatenation, except that they may not contain
substitutions. This means that you can't nest substitutions inside
other substitutions, and you can't have substitutions in keys.

When concatenating the path expression, any `.` characters outside
quoted strings are understood as path separators, while inside
quoted strings `.` has no special meaning. So
`foo.bar."hello.world"` would be a path with three elements,
looking up key `foo`, key `bar`, then key `hello.world`.

The main tricky point is that `.` characters in numbers do count
as a path separator. When dealing with a number as part of a path
expression, it's essential to retain the _original_ string
representation of the number as it appeared in the file (rather
than converting it back to a string with a generic
number-to-string library function).

 - `10.0foo` is a number then unquoted string `foo` and should
   be the two-element path with `10` and `0foo` as the elements.
 - `foo10.0` is an unquoted string with a `.` in it, so this would
   be a two-element path with `foo10` and `0` as the elements.
 - `foo"10.0"` is an unquoted then a quoted string which are
   concatenated, so this is a single-element path.
 - `1.2.3` is the three-element path with `1`,`2`,`3`

Unlike value concatenations, path expressions are _always_
converted to a string, even if they are just a single value.

If you have an array or element value consisting of the single
value `true`, it's a value concatenation and retains its character
as a boolean value.

If you have a path expression (in a key or substitution) then it
must always be converted to a string, so `true` becomes the string
that would be quoted as `"true"`.

If a path element is an empty string, it must always be quoted.
That is, `a."".b` is a valid path with three elements, and the
middle element is an empty string. But `a..b` is invalid and
should generate an error. Following the same rule, a path that
starts or ends with a `.` is invalid and should generate an error.

## Paths as keys

If a key is a path expression with multiple elements, it is
expanded to create an object for each path element other than the
last. The last path element, combined with the value, becomes a
field in the most-nested object.

In other words:

    foo.bar : 42

is equivalent to:

    foo { bar : 42 }

and:

    foo.bar.baz : 42

is equivalent to:

    foo { bar { baz : 42 } }

and so on. These values are merged in the usual way; which implies
that:

    a.x : 42, a.y : 43

is equivalent to:

    a { x : 42, y : 43 }

Because path expressions work like value concatenations, you can
have whitespace in keys:

    a b c : 42

is equivalent to:

    "a b c" : 42

Because path expressions are always converted to strings, even
single values that would normally have another type become
strings.

   - `true : 42` is `"true" : 42`
   - `3 : 42` is `"3" : 42`
   - `3.14 : 42` is `"3" : { "14" : 42 }`

As a special rule, the unquoted string `include` may not begin a
path expression in a key, because it has a special interpretation
(see below).

## Substitutions

Substitutions are a way of referring to other parts of the
configuration tree.

The syntax is `${pathexpression}` or `${?pathexpression}` where
the `pathexpression` is a path expression as described above. This
path expression has the same syntax that you could use for an
object key.

The `?` in `${?pathexpression}` must not have whitespace before
it; the three characters `${?` must be exactly like that, grouped
together.

For substitutions which are not found in the configuration tree,
implementations may try to resolve them by looking at system
environment variables or other external sources of configuration.
(More detail on environment variables in a later section.)

Substitutions are not parsed inside quoted strings. To get a
string containing a substitution, you must use value concatenation
with the substitution in the unquoted portion:

    key : ${animal.favorite} is my favorite animal

Or you could quote the non-substitution portion:

    key : ${animal.favorite}" is my favorite animal"

Substitutions are resolved by looking up the path in the
configuration. The path begins with the root configuration object,
i.e. it is "absolute" rather than "relative."

Substitution processing is performed as the last parsing step, so
a substitution can look forward in the configuration. If a
configuration consists of multiple files, it may even end up
retrieving a value from another file.

If a key has been specified more than once, the substitution will
always evaluate to its latest-assigned value (that is, it will
evaluate to the merged object, or the last non-object value that
was set, in the entire document being parsed including all
included files).

If a configuration sets a value to `null` then it should not be
looked up in the external source. Unfortunately there is no way to
"undo" this in a later configuration file; if you have `{ "HOME" :
null }` in a root object, then `${HOME}` will never look at the
environment variable. There is no equivalent to JavaScript's
`delete` operation in other words.

If a substitution does not match any value present in the
configuration and is not resolved by an external source, then it
is undefined. An undefined substitution with the `${foo}` syntax
is invalid and should generate an error.

If a substitution with the `${?foo}` syntax is undefined:

 - if it is the value of an object field then the field should not
   be created. If the field would have overridden a previously-set
   value for the same field, then the previous value remains.
 - if it is an array element then the element should not be added.
 - if it is part of a value concatenation with another string then
   it should become an empty string; if part of a value
   concatenation with an object or array it should become an empty
   object or array.
 - `foo : ${?bar}` would avoid creating field `foo` if `bar` is
   undefined. `foo : ${?bar}${?baz}` would also avoid creating the
   field if _both_ `bar` and `baz` are undefined.

Substitutions are only allowed in field values and array
elements (value concatenations), they are not allowed in keys or
nested inside other substitutions (path expressions).

A substitution is replaced with any value type (number, object,
string, array, true, false, null). If the substitution is the only
part of a value, then the type is preserved. Otherwise, it is
value-concatenated to form a string.

### Self-Referential Substitutions

The big picture:

 - substitutions normally "look forward" and use the final value
   for their path expression
 - when this would create a cycle, when possible the cycle must be
   broken by looking backward only (thus removing one of the
   substitutions that's a link in the cycle)

The idea is to allow a new value for a field to be based on the
older value:

    path : "a:b:c"
    path : ${path}":d"

A _self-referential field_ is one which:

 - has a substitution, or value concatenation containing a
   substitution, as its value
 - where this field value refers to the field being defined,
   either directly or by referring to one or more other
   substitutions which eventually point back to the field being
   defined

Examples of self-referential fields:

 - `a : ${a}`
 - `a : ${a}bc`
 - `path : ${path} [ /usr/bin ]`

Note that an object or array with a substitution inside it is
_not_ considered self-referential for this purpose. The
self-referential rules do _not_ apply to:

 - `a : { b : ${a} }`
 - `a : [${a}]`

These cases are unbreakable cycles that generate an error. (If
"looking backward" were allowed for these, something like
`a={ x : 42, y : ${a.x} }` would look backward for a
nonexistent `a` while resolving `${a.x}`.)

A possible implementation is:

 - substitutions are resolved by looking up paths in a document.
   Cycles only arise when the lookup document is an ancestor
   node of the substitution node.
 - while resolving a potentially self-referential field (any
   substitution or value concatenation that contains a
   substitution), remove that field and all fields which override
   it from the lookup document.

The simplest form of this implementation will report a circular
reference as missing; in `a : ${a}` you would remove `a : ${a}`
while resolving `${a}`, leaving an empty document to look up
`${a}` in. You can give a more helpful error message if, rather
than simply removing the field, you leave a marker value
describing the cycle. Then generate an error if you return to that
marker value during resolution.

Cycles should be treated the same as a missing value when
resolving an optional substitution (i.e. the `${?foo}` syntax).
If `${?foo}` refers to itself then it's as if it referred to a
nonexistent value.

### The `+=` field separator

Fields may have `+=` as a separator rather than `:` or `=`. A
field with `+=` transforms into a self-referential array
concatenation, like this:

    a += b

becomes:

    a = ${?a} [b]

`+=` appends an element to a previous array. If the previous value
was not an array, an error will result just as it would in the
long form `a = ${?a} [b]`. Note that the previous value is
optional (`${?a}` not `${a}`), which allows `a += b` to be the
first mention of `a` in the file (it is not necessary to have `a =
[]` first).

### Examples of Self-Referential Substitutions

In isolation (with no merges involved), a self-referential field
is an error because the substitution cannot be resolved:

    foo : ${foo} // an error

When `foo : ${foo}` is merged with an earlier value for `foo`,
however, the substitution can be resolved to that earlier value.
When merging two objects, the self-reference in the overriding
field refers to the overridden field. Say you have:

    foo : { a : 1 }

and then:

    foo : ${foo}

Then `${foo}` resolves to `{ a : 1 }`, the value of the overridden
field.

It would be an error if these two fields were reversed, so first:

    foo : ${foo}

and then second:

    foo : { a : 1 }

Here the `${foo}` self-reference comes before `foo` has a value,
so it is undefined, exactly as if the substitution referenced a
path not found in the document.

Because `foo : ${foo}` conceptually looks to previous definitions
of `foo` for a value, the error should be treated as "undefined"
rather than "intractable cycle"; as a result, the optional
substitution syntax `${?foo}` does not create a cycle:

    foo : ${?foo} // this field just disappears silently

If a substitution is hidden by a value that could not be merged
with it (by a non-object value) then it is never evaluated and no
error will be reported. So for example:

    foo : ${does-not-exist}
    foo : 42

In this case, no matter what `${does-not-exist}` resolves to, we
know `foo` is `42`, so `${does-not-exist}` is never evaluated and
there is no error. The same is true for cycles like `foo : ${foo},
foo : 42`, where the initial self-reference must simply be ignored.

A self-reference resolves to the value "below" even if it's part
of a path expression. So for example:

    foo : { a : { c : 1 } }
    foo : ${foo.a}
    foo : { a : 2 }

Here, `${foo.a}` would refer to `{ c : 1 }` rather than `2` and so
the final merge would be `{ a : 2, c : 1 }`.

Recall that for a field to be self-referential, it must have a
substitution or value concatenation as its value. If a field has
an object or array value, for example, then it is not
self-referential even if there is a reference to the field itself
inside that object or array.

Implementations must be careful to allow objects to refer to paths
within themselves, for example:

    bar : { foo : 42,
            baz : ${bar.foo}
          }

Here, if an implementation resolved all substitutions in `bar` as
part of resolving the substitution `${bar.foo}`, there would be a
cycle. The implementation must only resolve the `foo` field in
`bar`, rather than recursing the entire `bar` object.

Because there is no inherent cycle here, the substitution must
"look forward" (including looking at the field currently being
defined). To make this clearer, `bar.baz` would be `43` in:

    bar : { foo : 42,
            baz : ${bar.foo}
          }
    bar : { foo : 43 }

Mutually-referring objects should also work, and are not
self-referential (so they look forward):

    // bar.a should end up as 4
    bar : { a : ${foo.d}, b : 1 }
    bar.b = 3
    // foo.c should end up as 3
    foo : { c : ${bar.b}, d : 2 }
    foo.d = 4

Another tricky case is an optional self-reference in a value
concatenation, in this example `a` should be `foo` not `foofoo`
because the self reference has to "look back" to an undefined `a`:

    a = ${?a}foo

In general, in resolving a substitution the implementation must:

 - lazy-evaluate the substitution target so there's no
   "circularity by side effect"
 - "look forward" and use the final value for the path
   specified in the substitution
 - if a cycle results, the implementation must "look back"
   in the merge stack to try to resolve the cycle
 - if neither lazy evaluation nor "looking only backward" resolves
   a cycle, the substitution is missing which is an error unless
   the `${?foo}` optional-substitution syntax was used.

For example, this is not possible to resolve:

    bar : ${foo}
    foo : ${bar}

A multi-step loop like this should also be detected as invalid:

    a : ${b}
    b : ${c}
    c : ${a}

Some cases have undefined behavior because the behavior depends on
the order in which two fields are resolved, and that order is not
defined. For example:

    a : 1
    b : 2
    a : ${b}
    b : ${a}

Implementations are allowed to handle this by setting both `a` and
`b` to 1, setting both to `2`, or generating an error. Ideally
this situation would generate an error, but that may be difficult
to implement. Making the behavior defined would require always
working with ordered maps rather than unordered maps, which is too
constraining. Implementations only have to track order for
duplicate instances of the same field (i.e. merges).

Implementations must set both `a` and `b` to the same value in
this case, however. In practice this means that all substitutions
must be memoized (resolved once, with the result
retained). Memoization should be keyed by the substitution
"instance" (the specific occurrence of the `${}` expression)
rather than by the path inside the `${}` expression, because
substitutions may be resolved differently depending on their
position in the file.

## Includes

### Include syntax

An _include statement_ consists of the unquoted string `include`
followed by whitespace and then either:

 - a single _quoted_ string which is interpreted heuristically as
   URL, filename, or classpath resource.
 - `url()`, `file()`, or `classpath()` surrounding a quoted string
   which is then interpreted as a URL, file, or classpath. The
   string must be quoted, unlike in CSS.
 - `required()` surrounding one of the above

An include statement can appear in place of an object field.

If the unquoted string `include` appears at the start of a path
expression where an object key would be expected, then it is not
interpreted as a path expression or a key.

Instead, the next value must be a _quoted_ string or a quoted
string surrounded by `url()`, `file()`, or `classpath()`.
This value is the _resource name_.

Together, the unquoted `include` and the resource name substitute
for an object field syntactically, and are separated from the
following object fields or includes by the usual comma (and as
usual the comma may be omitted if there's a newline).

If an unquoted `include` at the start of a key is followed by
anything other than a single quoted string or the
`url("")`/`file("")`/`classpath("")` syntax, it is invalid and an
error should be generated.

There can be any amount of whitespace, including newlines, between
the unquoted `include` and the resource name. For `url()` etc.,
whitespace is allowed inside the parentheses `()` (outside of the
quotes).

Value concatenation is NOT performed on the "argument" to
`include` or `url()` etc. The argument must be a single quoted
string. No substitutions are allowed, and the argument may not be
an unquoted string or any other kind of value.

Unquoted `include` has no special meaning if it is not the start
of a key's path expression.

It may appear later in the key:

    # this is valid
    { foo include : 42 }
    # equivalent to
    { "foo include" : 42 }

It may appear as an object or array value:

    { foo : include } # value is the string "include"
    [ include ]       # array of one string "include"

You can quote `"include"` if you want a key that starts with the
word `"include"`, only unquoted `include` is special:

    { "include" : 42 }

### Include semantics: merging

An _including file_ contains the include statement and an
_included file_ is the one specified in the include statement.
(They need not be regular files on a filesystem, but assume they
are for the moment.)

An included file must contain an object, not an array. This is
significant because both JSON and HOCON allow arrays as root
values in a document.

If an included file contains an array as the root value, it is
invalid and an error should be generated.

The included file should be parsed, producing a root object. The
keys from the root object are conceptually substituted for the
include statement in the including file.

 - If a key in the included object occurred prior to the include
   statement in the including object, the included key's value
   overrides or merges with the earlier value, exactly as with
   duplicate keys found in a single file.
 - If the including file repeats a key from an earlier-included
   object, the including file's value would override or merge
   with the one from the included file.

### Include semantics: substitution

Substitutions in included files are looked up at two different
paths; first, relative to the root of the included file; second,
relative to the root of the including configuration.

Recall that substitution happens as a final step, _after_
parsing. It should be done for the entire app's configuration, not
for single files in isolation.

Therefore, if an included file contains substitutions, they must
be "fixed up" to be relative to the app's configuration root.

Say for example that the root configuration is this:

    { a : { include "foo.conf" } }

And "foo.conf" might look like this:

    { x : 10, y : ${x} }

If you parsed "foo.conf" in isolation, then `${x}` would evaluate
to 10, the value at the path `x`. If you include "foo.conf" in an
object at key `a`, however, then it must be fixed up to be
`${a.x}` rather than `${x}`.

Say that the root configuration redefines `a.x`, like this:

    {
        a : { include "foo.conf" }
        a : { x : 42 }
    }

Then the `${x}` in "foo.conf", which has been fixed up to
`${a.x}`, would evaluate to `42` rather than to `10`.
Substitution happens _after_ parsing the whole configuration.

However, there are plenty of cases where the included file might
intend to refer to the application's root config. For example, to
get a value from a system property or from the reference
configuration. So it's not enough to only look up the "fixed up"
path, it's necessary to look up the original path as well.

### Include semantics: missing files and required files

By default, if an included file does not exist then the include statement should
be silently ignored (as if the included file contained only an
empty object).

If however an included resource is mandatory then the name of the
included resource may be wrapped with `required()`, in which case
file parsing will fail with an error if the resource cannot be resolved.

The syntax for this is

    include required("foo.conf")
    include required(file("foo.conf"))
    include required(classpath("foo.conf"))
    include required(url("http://localhost/foo.conf"))


Other IO errors probably should not be ignored but implementations
will have to make a judgment which IO errors reflect an ignorable
missing file, and which reflect a problem to bring to the user's
attention.

### Include semantics: file formats and extensions

Implementations may support including files in other formats.
Those formats must be compatible with the JSON type system, or
have some documented mapping to JSON's type system.

If an implementation supports multiple formats, then the extension
may be omitted from the name of included files:

    include "foo"

If a filename has no extension, the implementation should treat it
as a basename and try loading the file with all known extensions.

If the file exists with multiple extensions, they should _all_ be
loaded and merged together.

Files in HOCON format should be parsed last. Files in JSON format
should be parsed next-to-last.

In short, `include "foo"` might be equivalent to:

    include "foo.properties"
    include "foo.json"
    include "foo.conf"

This same extension-based behavior is applied to classpath
resources and files.

For URLs, a basename without extension is not allowed; only the
exact URL specified is used. The format will be chosen based on
the Content-Type if available, or by the extension of the path
component of the URL if no Content-Type is set. This is true even
for file: URLs.

### Include semantics: locating resources

A quoted string not surrounded by `url()`, `file()`, `classpath()`
must be interpreted heuristically. The heuristic is to treat the
quoted string as:

 - a URL, if the quoted string is a valid URL with a known
   protocol.
 - otherwise, a file or other resource "adjacent to" the one being
   parsed and of the same type as the one being parsed. The meaning
   of "adjacent to", and the string itself, has to be specified
   separately for each kind of resource.
 - On the Java Virtual Machine, if an include statement does not
   identify a valid URL or an existing resource "adjacent to" the
   including resource, implementations may wish to fall back to a
   classpath resource.  This allows configurations found in files
   or URLs to access classpath resources in a natural way.

Implementations may vary in the kinds of resources they can
include.

For resources located on the Java classpath:

 - included resources are looked up by calling `getResource()` on
   the same class loader used to look up the including resource.
 - if the included resource name is absolute (starts with '/')
   then it should be passed to `getResource()` with the '/'
   removed.
 - if the included resource name does not start with '/' then it
   should have the "directory" of the including resource
   prepended to it, before passing it to `getResource()`.  If the
   including resource is not absolute (no '/') and has no "parent
   directory" (is just a single path element), then the included
   relative resource name should be left as-is.
 - it would be wrong to use `getResource()` to get a URL and then
   locate the included name relative to that URL, because a class
   loader is not required to have a one-to-one mapping between
   paths in its URLs and the paths it handles in `getResource()`.
   In other words, the "adjacent to" computation should be done
   on the resource name not on the resource's URL.

For plain files on the filesystem:

 - if the included file is an absolute path then it should be kept
   absolute and loaded as such.
 - if the included file is a relative path, then it should be
   located relative to the directory containing the including
   file.  The current working directory of the process parsing a
   file must NOT be used when interpreting included paths.
 - if the file is not found, fall back to the classpath resource.
   The classpath resource should not have any package name added
   in front, it should be relative to the "root"; which means any
   leading "/" should just be removed (absolute is the same as
   relative since it's root-relative). The "/" is handled for
   consistency with including resources from inside other
   classpath resources, where the resource name may not be
   root-relative and "/" allows specifying relative to root.

URLs:

 - for files loaded from a URL, "adjacent to" should be based
   on parsing the URL's path component, replacing the last
   path element with the included name.
 - file: URLs should behave in exactly the same way as a plain
   filename

Implementations need not support files, Java resources, or URLs;
and they need not support particular URL protocols. However, if
they do support them they should do so as described above.

Note that at present, if `url()`/`file()`/`classpath()` are
specified, the included items are NOT interpreted relative to the
including items. Relative-to-including-file paths only work with
the heuristic `include "foo.conf"`. This may change in the future.

## Conversion of numerically-indexed objects to arrays

In some file formats and contexts, such as Java properties files,
there isn't a good way to define arrays. To provide some mechanism
for this, implementations should support converting objects with
numeric keys into arrays. For example, this object:

    { "0" : "a", "1" : "b" }

could be treated as:

    [ "a", "b" ]

This allows creating an array in a properties file like this:

    foo.0 = "a"
    foo.1 = "b"

The details:

 - the conversion should be done lazily when required to avoid
   a type error, NOT eagerly anytime an object has numeric
   keys.
 - the conversion should be done when you would do an automatic
   type conversion (see the section "Automatic type conversions"
   below).
 - the conversion should be done in a concatenation when a list
   is expected and an object with numeric keys is found.
 - the conversion should not occur if the object is empty or
   has no keys which parse as positive integers.
 - the conversion should ignore any keys which do not parse
   as positive integers.
 - the conversion should sort by the integer value of each
   key and then build the array; if the integer keys are "0" and
   "2" then the resulting array would have indices "0" and "1",
   i.e. missing indices in the object are eliminated.
