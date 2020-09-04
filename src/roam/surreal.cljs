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

(defn walk-js-data-structures
  [inner outer form]
  (cond
    (array? form) (outer (.map form (fn [v]
                                      (inner v))))
    ;; todo figure out if this can be made faster
    (object? form) (outer (js/Object.fromEntries
                            (.map (js/Object.entries form)
                              (fn [kv]
                                (outer
                                  #js [(inner (aget kv 0))
                                       (inner (aget kv 1))])
                                ))))
    :else form))

(defn postwalk-js-data-structures
  [f form]
  (walk-js-data-structures (partial postwalk-js-data-structures f) f form))

(comment
  (postwalk-js-data-structures
    (fn [x] (js/console.log "walk-js" x) x)
    (clj->js {:1 {:2   [1 2 3]
                  :2.1 [4]}}))

  (require '[clojure.walk])
  (clojure.walk/postwalk
    (fn [x] (when (coll? x) (println "walk-cljs" x)) x)
    {:1 {:2   [1 2 3]
         :2.1 [4]}})
  )

(defn walk-js-objects
  [inner outer form]
  (cond
    (array? form) (.map form (fn [v]
                               (inner v)))
    ;; todo figure out if this can be made faster
    (instance? js/Object form) (outer (js/Object.fromEntries
                                        (.map (js/Object.entries form)
                                          (fn [kv]
                                            #js [(aget kv 0)
                                                 (inner (aget kv 1))]
                                            ))))
    :else form))

(defn postwalk-js-objects
  [f form]
  (walk-js-objects (partial postwalk-js-objects f) f form))

(defn strip
  "Removes the keyvals of CLJS datastructures that may be incompatible with
  document stores like Firebase. Use hydrate to restore."
  [^js obj]
  (postwalk-js-objects
    (fn [x]
      (if (instance? js/Object x)
        (doto x
          (gobj/remove "cljs$lang$protocol_mask$partition0$")
          (gobj/remove "cljs$lang$protocol_mask$partition1$"))
        x
        ))
    obj))

(defn hydrate
  "Walks a JavaScript object of instance-method-stripped CLJS data-structures (e.g. read
  from indexedDB, Firebase or sent over a webworker) and restores them."
  [^js obj]
  (postwalk-js-data-structures hydrate-obj obj))

(comment

  (js/console.log (strip {:1 {:2 [1 2 3]}}))
  ;; strip + roundtrip
  (let [v      {:1 {:2 [1 2 3]}}
        stripv (strip v)
        jsonv  (js/JSON.stringify stripv)
        parsev (js/JSON.parse jsonv)
        hv     (hydrate parsev)
        equal? (= v hv)]
    (js/console.log "roundtrip-result" hv)
    (js/console.log "works in cljs" (clj->js (update-in hv [:1 :2] conj 4)))
    (js/console.log "equal?" equal?)
    (js/console.log "out type" (pr-str (type hv)))
    hv)
  )