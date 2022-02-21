(ns proj.ui
  (:require
    [proj.data :as data]
    ["react-twemoji$default" :as Twemoji]
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(defonce state (r/atom {}))

(defn create-project [_ev]
  (swap! state assoc :project (data/make-project)))

(defn create-task [tasks]
  (swap! tasks conj (data/make-task)))

(defn remove-task [tasks id]
  (swap! tasks (fn [old-tasks]
                 (vec
                   (remove #(= id (:id %)) old-tasks)))))

(defn component-task-header []
  [:tr
   [:td ""]
   [:td "Task"]
   [:td "Who"]
   [:td "Progress"]
   [:td "Parent"]
   [:td "Days"]
   [:td "Start"]
   [:td "End"]
   [:td ""]])

(defn component-task-edit [start tasks idx task]
  (let [[start end] (data/compute-date-range start @tasks task idx)]
    [:tr
     [:td (inc idx)]
     [:td [:input (data/editable tasks [idx :task])]]
     [:td [:input (data/editable tasks [idx :who])]]
     [:td [:input (data/editable tasks [idx :progress]
                                 {:type "number"
                                  :min 0
                                  :max 100})]]
     [:td [:input (data/editable tasks [idx :parent]
                                 {:type "number"
                                  :min 0
                                  :max 1000})]]
     [:td [:input (data/editable tasks [idx :duration]
                                 {:type "number"
                                  :min 0
                                  :max 1000})]]
     [:td [:span start]]
     [:td [:span end]]
     [:td [:button {:on-click #(remove-task tasks (:id task))} "X"]]]))

(defn component-tasks-table [start tasks]
  [identity
   [:div
    [:table
     [:thead
      [component-task-header]]
     [:tbody
      (doall
        (for [idx (range (count @tasks))]
          (let [task (nth @tasks idx)]
            (with-meta [component-task-edit start tasks idx task] {:key (:id task)}))))]]
    [:button {:on-click #(create-task tasks)} "Add task"]]])

(defn component-home [_state]
  [:div#landing
   [:h1 "Michaelsoft Project"]
   [:p "A simple Gantt chart planner. No sign up required."]
   [:p
    [:button {:on-click create-project} "new project"]]
   [:p]
   [:> Twemoji "🤠"]
   ;[:pre (pr-str @state)]
   ;[:p [:a {:href "/mypage"} "Static server rendered page."]]
   ;[:p [:a {:href "/api/example.json"} "JSON API example."]]
   ])

(defn component-project [state]
  (let [project (r/cursor state [:project])
        tasks (r/cursor state [:project :tasks])
        start (get-in @state [:project :start])]
    [:div
     [:h1 [:input (data/editable project [:title] {:placeholder "Untitled project"})]]
     [:h2 [:input (data/editable project [:company] {:placeholder "Company name"})]]
     [:h3 [:input (data/editable project [:lead] {:placeholder "Project lead"})]]
     [:p [:label "Start: " [:input (data/editable project [:start]
                                                  {:type "date"
                                                   :defaultValue (data/today)})]]]
     [component-tasks-table start tasks]
     [:pre (pr-str @tasks)]]))

(defn component-main [state]
  (if (@state :project)
    [component-project state]
    [component-home state]))

(defn start {:dev/after-load true} []
  (rdom/render [component-main state]
               (js/document.getElementById "app")))

(defn main! []
  (start))
