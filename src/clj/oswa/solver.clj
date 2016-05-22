(ns oswa.solver
  (:require [clojure-csv.core :refer [parse-csv]]
            [org-struct.core :refer [org-struct]]
            [clojure.xml :as xml]
            [clojure.java.io :as io]
            [clojure.string :refer [starts-with?]]))

(defn p [x & args]
  (apply prn x args)
  x)

(defn parse-xml-file [f]
  (with-open [s (io/input-stream f)]
    (xml/parse s)))

(defn find-tags [tag xml]
  (filter #(= (:tag %) tag) xml))

(defn find-tag [tag xml]
  (first (find-tags tag xml)))

(defn str-in-tag [tag tags]
  (first (:content (find-tag tag tags))))

(defn parse-attrs [attrs]
  (into {} (for [attr (map :content attrs)]
             {(str-in-tag :Alias attr) (str-in-tag :FieldID attr)})))

(defn numbers [xs]
  (map #(Double/parseDouble %) xs))

(defn find-attr [attr xml attrs]
  (if-let [id (attrs attr)]
    (->> (find-tags :ExtendedAttribute xml)
         (map :content)
         (filter #(= (str-in-tag :FieldID %) id))
         first
         (str-in-tag :Value))
    (throw (Exception. (str "No attr " attr " found in " attrs)))))

(defn find-attrs [prefix xml attrs]
  (let [ks (->> (keys attrs)
                (filter #(and (string? %) (starts-with? % prefix)))
                (map #(subs % (count prefix)))
                (map #(Integer/parseInt %))
                sort)]
    (if-not (= ks (range 1 (inc (count ks))))
      (throw (Exception. (str "Incorrect sequence of " prefix ": " ks))))
    (map #(find-attr (str prefix %) xml attrs) ks)))

(defn read-tasks [file]
  (let [xml (parse-xml-file file)
        content (:content xml)
        tasks (find-tag :Tasks content)
        attrs (parse-attrs (:content (find-tag :ExtendedAttributes content)))]
    (for [task (map :content (:content tasks))
          :let [id (str-in-tag :UID task)]
          :when (not= id "0")]
      {:id id
       :name (str-in-tag :Name task)
       :weight (Double/parseDouble (find-attr "Weight" task attrs))
       :times (numbers (find-attrs "Time" task attrs))
       :finances (numbers (find-attrs "Finances" task attrs))
       :qualities (numbers (find-attrs "Quality" task attrs))
       :dependencies (map #(->> % :content (str-in-tag :PredecessorUID))
                          (find-tags :PredecessorLink task))})))

(defn solve [time quality finances file]
  (let [tasks (read-tasks file)
        w (map :weight tasks)
        t (map :times tasks)
        f (map :finances tasks)
        q (map :qualities tasks)
        task-indexes (into {} (map-indexed #(vector (:id %2) (inc %1)) tasks))
        graph (mapcat (fn [task]
                        (for [d (:dependencies task)]
                          [(task-indexes d) (task-indexes (:id task))]))
                      tasks)]
    (org-struct [time finances quality] w t f q graph)))
