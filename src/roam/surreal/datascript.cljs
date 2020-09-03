(ns roam.surreal.datascript
  (:require [datascript.db :as db]
            [me.tonsky.persistent-sorted-set :as bset]))

(defn prep-datascript-db [db]
  (into {}
    (-> db
      (update :eavt #(into [] %))
      (update :aevt #(into [] %))
      (update :avet #(into [] %))
      (update :hash deref))))

(defn recover-datascript-db [db]
  (-> db
    (update :eavt #(into (bset/sorted-set-by datascript.db/cmp-datoms-eavt) %))
    (update :aevt #(into (bset/sorted-set-by datascript.db/cmp-datoms-aevt) %))
    (update :avet #(into (bset/sorted-set-by datascript.db/cmp-datoms-avet) %))
    (update :hash atom)
    db/map->DB))
