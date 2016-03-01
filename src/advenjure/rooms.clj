(ns advenjure.rooms
  (:require [advenjure.items :refer [iname describe-container]]))

(defrecord Room [name description])

(defn make [name description & extras]
  (map->Room (merge {:name name :description description}
                    {:intial-description description ;default args, can be overriden by extras
                     :items #{} :item-descriptions {} :visited false}
                    (apply hash-map extras))))

(defn add-item
  ([room item]
   (assoc room :items (conj (:items room) item)))
  ([room item description]
   (-> room
       (assoc :items (conj (:items room) item))
       (assoc-in [:item-descriptions (name item)] description))))

(defn describe [room]
  (let [room-descr (if (:visited room)
                    (:description room)
                    (:initial-description room))
        item-descr (clojure.string/join "\n"
                    (for [item (:items room)]
                      (str "There's a " (first (:names item)) " here. "
                        (describe-container item))))]
    (str room-descr "\n" item-descr)))


; ROOM MAP BUILDING
(defn add-room [room-map k room]
  "Add the given room to the room-map under the k key."
  (def defaults {:visited false
                 :items {}})
  (assoc room-map k (merge room defaults)))

(def matching-directions {:north :south
                          :northeast :southwest
                          :east :west
                          :southeast :northwest
                          :south :north
                          :southwest :northeast
                          :west :east
                          :northwest :southeast
                          :up :down
                          :down :up})

(defn connect-rooms [room-map r1 direction r2]
  "Connect r1 with r2 in the given direction and make the corresponding
  connection in r2."
  (-> room-map
      (assoc-in [r1 direction] r2)
      (assoc-in [r2 (get matching-directions direction)] r1)))

; TODO one way connection

;;; ROOM DEFINITIONS

; TODO convert to records

(def magazine {:names ["sports magazine" "magazine"]
               :description "The cover reads 'Sports Almanac 1950-2000'"
               :read "It's actually a naughty magazine once you remove the dust cover."
               :take true})


(def bedroom {:name "Bedroom"
              :intial-description "I woke up in a smelling little bedroom,
                                without windows or any furniture other than the bed I was laying in and a reading lamp."
              :description "A smelling bedroom. There was an unmade bed near the corner and a lamp by the bed."
              :items #{{:names ["bed"] :description "It was the bed I slept in."}
                       {:names ["reading lamp" "lamp"] :description "Nothing special about the lamp."}
                       magazine}
              :item-descriptions {"bed" "" ;empty means skip it while describing, already contained in room description
                                  "magazine" "Laying by the bed was a sports magazine."}}) ; use this when describing the room instead of "there's a X here"


(def drawer {:names ["drawer" "drawers" "chest" "chest drawer" "chest drawers" "drawer chest" "drawers chest"]
             :closed true ; if it has closed kw, means it responds to open/close.
             :items #{} ; if it has items set, means it can contain stuff.
             :description "A chest drawer"})


(def living {:name "Living Room"
             :intial-description "The living room was as smelly as the bedroom, and although there was a window,
                                it appeared to be nailed shut. There was a pretty good chance I'd choke to death
                                if I didn't leave the place soon."
             :description "A living room with a nailed shut window."})


;;; BUILD THE ACTUAL MAP
(def room-map (-> {}
                  (add-room :bedroom bedroom)
                  (add-room :living living)
                  (connect-rooms :bedroom :north :living)))
