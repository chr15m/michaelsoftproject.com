(ns proj.data
  (:require
    [sitefox.ui :refer [log]]))

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

(defn sum-parent-duration [durations *tasks task depth]
  (let [parent-idx (:parent task)
        parent-task (when (and parent-idx (> (int parent-idx) 0))
                      (nth *tasks (dec (int parent-idx))))]
    (if (and parent-task (< depth (count *tasks)))
      (conj (sum-parent-duration durations *tasks parent-task (inc depth)) (int (:duration parent-task)))
      nil)))

(defn compute-date-range [start *tasks task]
  (let [duration (int (:duration task))
        parent-durations (sum-parent-duration [] *tasks task 0)
        parents-duration (apply + parent-durations)]
    ; show an error if recursion depth exceeded task count
    (if (= (count *tasks) (count parent-durations))
      [:error :error]
      [(end-date start parents-duration) (end-date start (+ parents-duration duration))])))

(defn make-id [n]
  (apply str (map (fn [_] (.toString (rand-int 16) 16)) (range n))))

(defn make-task []
  {:id (make-id 16)
   :task ""
   :who ""
   :progress 0
   :parent nil
   :duration 0})

(defn make-project []
  {:id (make-id 16)
   :title nil
   :company nil
   :lead nil
   :start nil
   :tasks []})

(defn editable
  [parent k & [extra]]
  (merge extra
         {:on-change #(swap! parent assoc-in k (-> % .-target .-value))
          :value (get-in @parent k)}))
