# surreal

Performance optimized CLJS<->JSON serialization library. 

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
- [ ] allow users to extend `hydrate-obj` with additional data-matchers/transformers
- [ ] setup proper cljs tests

## License

Copyright © 2020 Dennis Heihoff

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
