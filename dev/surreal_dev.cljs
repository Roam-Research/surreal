(ns surreal-dev
  (:require [roam.surreal :as surr]
            [datascript.core :as d]
            [roam.surreal.datascript :as sd]))

(defn roundtrip [v]
  (println "----------ROUNDTRIP----------")
  (println :initial-value v)
  (js/console.log :initial-value v)
  (let [jsonv  (js/JSON.stringify v)
        parsev (js/JSON.parse jsonv)
        hv     (surr/hydrate parsev)
        equal? (= v hv)]
    (println :roundtrip-result hv)
    (js/console.log :roundtrip-result hv)
    (println :equal? equal?)
    (js/console.log :equal? equal?)
    hv))

(defn common-cljs-values-roundtrip []
  (roundtrip 1)
  (roundtrip 1.2)
  (roundtrip :sur)
  (roundtrip "real")
  (roundtrip true)
  (roundtrip false)
  (roundtrip nil)
  (roundtrip [])
  (roundtrip [1])
  (roundtrip #{1})
  (roundtrip {:sur :real})
  (roundtrip {:foo [:bar [1 2 3] #{:baz}]})
  (roundtrip [{:foo [:bar [1 2 3] #{:baz}]}]))

(defn datascript-db-roundtrip []
  (let [ids        (range -1 0)
        persons    (mapv
                     (fn [id]
                       {:db/id          id
                        :person/name    (str "Person " id)
                        :person/friends (-> (random-sample 0.2 ids)
                                          set
                                          (disj id)
                                          (->> (map (fn [id]
                                                      {:db/id id}))))})
                     ids)
        db         (->
                     (d/empty-db {:person/friends {:db/cardinality :db.cardinality/many}})
                     (d/db-with persons))
        prepped-db (sd/prep-datascript-db db)
        jsondb     (js/JSON.stringify prepped-db)
        parsed-db  (js/JSON.parse jsondb)
        hdb        (surr/hydrate parsed-db)
        recover-db (sd/recover-datascript-db hdb)
        equal?     (= db recover-db)]
    (println :dbs-equal? equal?)
    (js/console.log :dbs-equal? equal?)))

(defn init []
  (js/console.log "init")
  (common-cljs-values-roundtrip)
  (datascript-db-roundtrip))