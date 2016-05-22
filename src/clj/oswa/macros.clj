(ns oswa.macros)

(defmacro defsub [name [db & args] & body]
  `(re-frame.core/register-sub
     ~name
     (fn [db# [_# ~@args]]
       (reagent.ratom/reaction
         (let [~db (deref db#)]
           ~@body)))))

(defmacro defhandler [name [db & args] & body]
  `(re-frame.core/register-handler
     ~name
     (fn [~db [_# ~@args]]
       (do ~@body))))

(defn subs-let [subs]
  (vec (mapcat (fn [[name query]]
                 [name `(re-frame.core/subscribe ~query)])
               (partition 2 subs))))

(defmacro with-subs [subs & body]
  `(let ~(subs-let subs)
     ~@body))

(defmacro defcomponent [name args subs & body]
  `(defn ~name ~args
     (with-subs ~subs
       (fn ~name [] ~@body))))
