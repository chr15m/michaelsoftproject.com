(ns proj.ui
  (:require
    [proj.data :as data]
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(defonce state (r/atom {}))

(defn create-project [_ev]
  (swap! state assoc :project (data/make-project)))

(defn create-task [tasks]
  (let [task (data/make-task)]
    (swap! tasks assoc (:id task) task)))

(defn remove-task [tasks id]
  (swap! tasks dissoc id))

(defn component-task-edit [tasks id start duration]
  [:tr {:key id}
           [:td [:input (data/editable tasks [id :task])]]
           [:td [:input (data/editable tasks [id :who])]]
           [:td [:input (data/editable tasks [id :progress]
                                       {:type "number"
                                        :min 0
                                        :max 100})]]
           [:td [:input (data/editable tasks [id :duration]
                                       {:type "number"
                                        :min 0
                                        :max 1000})]]
           [:td [:input (data/editable tasks [id :start]
                                       {:type "date"
                                        :defaultValue (data/today)})]]
           [:td [:span (data/end-date start duration)]]
           [:td [:button {:on-click #(remove-task tasks id)} "X"]]])

(defn component-tasks-table [tasks]
  [identity
   [:div
    [:table
     [:thead
      [:tr
       [:td "Task"]
       [:td "Who"]
       [:td "Progress"]
       [:td "Days"]
       [:td "Start"]
       [:td "End"]
       [:td ""]]]
     [:tbody
      (doall
        (for [[id {:keys [start duration]}] @tasks]
          [component-task-edit tasks id start duration]))]]
    [:button {:on-click #(create-task tasks)} "Add task"]]])

(defn component-home [_state]
  [:div#landing
   [:h1 "Michaelsoft Project 🤠"]
   [:p "A simple Gantt chart planner. No sign up required."]
   [:p
    [:button {:on-click create-project} "new project"]]
   ;[:pre (pr-str @state)]
   ;[:p [:a {:href "/mypage"} "Static server rendered page."]]
   ;[:p [:a {:href "/api/example.json"} "JSON API example."]]
   ])

(defn component-project [state]
  (let [project (r/cursor state [:project])
        {:keys [title company lead]} @project
        tasks (r/cursor state [:project :tasks])]
    [:div
     [:h1 [:input (data/editable project [:title] {:placeholder "Untitled project"})]]
     [:h2 [:input (data/editable project [:company] {:placeholder "Company name"})]]
     [:h3 [:input (data/editable project [:lead] {:placeholder "Project lead"})]]
     [component-tasks-table tasks]]))

(defn component-main [state]
  (if (@state :project)
    [component-project state]
    [component-home state]))

(defn start {:dev/after-load true} []
  (rdom/render [component-main state]
               (js/document.getElementById "app")))

(defn main! []
  (start))
