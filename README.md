# surreal

**CLJS DataStructures <-> JSON hydration library.**

Surreal dispatches on as few predicates
of JS fields in serialized CLJS data-structures as possible
to identify and re-hydrate required functions and fields on the object
that have been stripped during JSON serialization.    

Surreal is solely about speed. This also acts as the
excuse for its otherwise less than ideal code.   

Works with IndexedDB directly (no JSON.stringify required).

## Examples

See `dev/examples.cljs`

## Status

PoC / Alpha. 

## Get started

```
yarn install
shadow-cljs watch dev
```

Open your browser on [http://localhost:8022/](http://localhost:8022/) and check the console.
See dev folder for examples.

### CLJS REPL
Shadow-cljs provides a CLJ REPL on `localhost:9002`.
Turn it into CLJS by running: 

```
(require '[shadow.cljs.devtools.api :as shadow])
   
(shadow/repl :dev)
```

## Todo

- [ ] add impls for more data-types
- [ ] add extension points for additional data-matchers/transformers to `hydrate-obj` 
- [ ] setup proper cljs tests

## License

Copyright © 2020 Dennis Heihoff

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
