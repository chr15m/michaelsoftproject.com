(ns proj.data)

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
   :tasks []})
