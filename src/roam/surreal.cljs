(ns roam.surreal
  (:require [datascript.core :as d]
            [goog.object :as gobj]))

(defn hydrate-obj [^js obj]
  (when obj
    (let [root         (.-root obj)
          meta         (.-meta obj)
          cnt          (.-cnt obj)
          hash         (.-__hash obj)
          meta-exists? (.hasOwnProperty obj "meta")]
      ;(js/console.log "obj" obj)
      (cond
        (string? obj) obj
        (number? obj) obj
        (and (.hasOwnProperty obj "ns") (.-name obj))
        (Keyword. (.-ns obj) (.-name obj) (.-fqn obj) (.-_hash obj))

        (.-tail obj) (do
                       ;(println "vec")
                       (PersistentVector.
                         meta
                         cnt
                         (.-shift obj)
                         (VectorNode.
                           (.-edit root)
                           (.-arr root))
                         (.-tail obj)
                         hash))

        (.-root obj) (do
                       ;(println "hash-map")
                       (PersistentHashMap.
                         meta
                         cnt
                         (if (.-cnt root)
                           (ArrayNode. (.-edit root) (.-cnt root) (.-arr root))
                           (BitmapIndexedNode.
                             (.-edit root)
                             (.-bitmap root)
                             (.-arr root)))
                         (.-has_nil_QMARK_ obj)
                         (.-nil_val obj)
                         hash))

        (and meta-exists? (.-hash_map obj))
        (do
          ;(println "hashset")
          (PersistentHashSet. meta (hydrate-obj (.-hash_map obj)) hash))

        (and meta-exists? cnt (.-arr obj))
        (do
          ;(println "array-map")
          (PersistentArrayMap.
            meta
            cnt
            (.-arr obj)
            hash))

        (and (.-tx obj) (.-e obj)) (do
                                     ;(println "datom")
                                     (let [d ^js (d/datom (.-e obj) (.-a obj) (.-v obj) (.-tx obj))]
                                       (gobj/set d (.-_hash obj))
                                       d
                                       ))

        :else (do
                ;(js/console.log "else" obj)
                obj)
        )))
  )

(defn walk-js
  [inner outer form]
  (cond
    (array? form) (outer (.map form (fn [v] (inner v))))
    (object? form) (outer (js/Object.fromEntries
                            (.map (js/Object.entries form)
                              (fn [kv]
                                #js [(inner (aget kv 0))
                                     (inner (aget kv 1))]
                                ))))
    :else (outer form)))

(defn postwalk-js
  [f form]
  (walk-js (partial postwalk-js f) f form))

(defn hydrate [^js obj]
  (postwalk-js hydrate-obj obj))
