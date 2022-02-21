(ns proj.data)

(defn today []
  (let [d (js/Date.)]
    (str
      (.getFullYear d)
      "-"
      (inc (.getMonth d))
      "-"
      (.getDate d))))

(defn end-date [start duration]
  (if start
    (-> start
        (js/Date.)
        (.getTime)
        (+ (* duration 1000 60 60 24))
        (js/Date.)
        (.toLocaleDateString))
    ""))

(defn make-id [n]
  (apply str (map (fn [_] (.toString (rand-int 16) 16)) (range n))))

(defn make-task []
  {:id (make-id 16)
   :task ""
   :who ""
   :progress 0
   :start nil
   :duration 0})

(defn make-project []
  {:id (make-id 16)
   :title nil
   :company nil
   :lead nil
   :tasks {}})

(defn editable
  [parent k & [extra]]
  (merge extra
         {:on-change #(swap! parent assoc-in k (-> % .-target .-value))
          :value (get-in @parent k)}))
