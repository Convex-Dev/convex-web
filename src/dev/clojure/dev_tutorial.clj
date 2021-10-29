(ns dev-tutorial)

(comment

  '(syntax
     "Text explaining something in the Sandbox."
     {:interact? true
      :cls? true})

  '(syntax
     [:text "Text explaining something in the Sandbox."]
     {:interact? true})

  '(syntax
     ["Text explaining something in the Sandbox.       "
      [:cmd
       {:show-source? true}
       "(inc 1)"]
      " Text explaining something in the Sandbox. "
      " Text explaining something in the Sandbox. "
      " Text explaining something in the Sandbox. "
      [:cmd
       {:show-source? true}
       "(inc 1)"]]
     {:interact? true})

  '(syntax
     [:h-box
      [:text "Text explaining something in the Sandbox."]
      [:text "Text explaining something in the Sandbox."]
      [:text "Text explaining something in the Sandbox."]
      [:text "Text explaining something in the Sandbox."]
      [:cmd
       "(inc 1)"]]
     {:interact? true})

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? false}
      "(inc 1)"]
     {:interact? true
      :mode :query
      :input "(inc 1)"})

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? true}
      "(inc 1)"]
     {:interact? true})


  ;; -- Lang

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :input [["x"] ["y"]]
       :lang (str '(fn [x & more] [x more]))}
      "nil"]
     {:interact? true
      :cls? true})

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? true
       :input [[:x] [:y]]
       :lang (str '(fn [_ & [x y]]
                     (let [n (max x y)]
                       (syntax
                         [:md (str "#### Max is:\n## " n)]
                         {:interact? true}))))}
      (str '(max x y))]
     {:interact? true
      :cls? true})

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? true
       :lang (str '(fn [x]
                     (syntax
                       [:v-box
                        [:code x]
                        [:cmd
                         {:mode :query
                          :name "Done"}
                         "nil"]]
                       {:interact? true
                        :cls? true})))}
      (str '(inc 1))]
     {:interact? true
      :cls? true})


  '(syntax
     [:md "# Title\n\n## Subtitle"]
     {:interact? true})


  ;; Tutorial example

  '(syntax
     [:v-box
      [:md
       "### Convex Lisp Tutorial\n\nLet's define a function `f`:"]

      [:cmd
       {:name "Execute"
        :show-source? true}
       "(defn f [x] x)"]

      [:text
       "Click on 'Continue' once you have executed the step above."]

      [:cmd
       {:mode :query
        :name "Continue"}
       (str '(syntax
               [:v-box
                [:text "Now let's call `f`, and pass `1` as argument:"]

                [:cmd
                 {:name "(f 1)"
                  :mode :query}

                 (str '(let [x (f 1)]
                         (syntax
                           [:v-box
                            [:text "Result:"]
                            [:code x]

                            [:cmd
                             {:name "Finish"
                              :mode :query}
                             ":Finish"]]

                           {:interact? true
                            :cls? false
                            :mode :query})))]]

               {:interact? true
                :cls? true
                :mode :query}))]]

     {:interact? true
      :cls? true
      :mode :query
      :input "(defn f [x] x)"})


  ;; -- Voting System
  ;; https://convex.world/examples/voting-system

  '(defn show-votes []
     (syntax
       [:v-box
        [:md "### Votes"]

        [:h-box
         [:md "**Yes:**"]
         [:text (get my-proposal/votes :yes)]]

        [:h-box
         [:md "**Meh:**"]
         [:text (get my-proposal/votes :meh)]]

        [:h-box
         [:md "**No:**"]
         [:text (get my-proposal/votes :no)]]]

       {:interact? true
        :cls? false}))

  '(defn demo1 []
     (syntax
       [:v-box

        ;; Markdown text.
        [:md "### What if instead of writing Convex Lisp to interface with a Smart Contract, you could, let's say, press buttons?"]

        ;; Command with nested Commands for each vote.
        [:cmd
         {:name "Let's see if that's possible!"}
         (str '(syntax
                 [:v-box
                  [:text "Do you like Belgian waffles?"]


                  ;; Each Command to vote does two things:
                  ;; 1) vote
                  ;; 2) show votes
                  ;;
                  ;; `show-votes` is another interactive syntax.

                  [:cmd
                   {:name "Yes"}
                   (str '(do
                           (call my-proposal (vote :yes))

                           (show-votes)))]

                  [:cmd
                   {:name "Meh"}
                   (str '(do
                           (call my-proposal (vote :meh))

                           (show-votes)))]

                  [:cmd
                   {:name "No"}
                   (str '(do
                           (call my-proposal (vote :no))

                           (show-votes)))]]

                 {:interact? true
                  :cls? true}))]]

       ;; It's a 'special' Syntax: it's interactive, and it clears the screen.
       {:interact? true
        :cls? true}))


  '(defn check-votes []
     (syntax
       [:cmd
        {:name "Check votes"}
        (str '(show-votes))]
       {:interact? true
        :cls? false}))


  ;; Convex Lisp tutorial based on 4Clojure
  ;; -- https://4clojure.oxal.org/#/problem/1

  '(let [back-to-menu [:cmd
                       {:name "Back to Menu"
                        :mode :query}
                       (str '(syntax (tutorial) {:interact? true
                                                 :cls? true}))]]

     (defn tutorial []
       (syntax
         [:v-box
          [:md "### Convex Lisp Tutorial"]

          [:cmd
           {:name "Tutorial 1"}
           (str '(tutorial-1))]

          [:cmd
           {:name "Tutorial 2"}
           (str '(tutorial-2))]

          [:cmd
           {:name "Tutorial 3"}
           (str '(tutorial-3))]

          [:cmd
           {:name "Tutorial 4"}
           (str '(tutorial-4))]]
         {:interact? true
          :cls? true}))

     (defn tutorial-1 []
       (syntax
         [:v-box
          [:md "### Problem 1"]

          [:md "Complete the expression so it will evaluate to true."]

          ;; Command to check the solution.
          [:cmd
           {:name "Check"
            :mode :query
            :show-source? true
            :input [[:x]]
            :lang
            (str
              '(fn [form & [x]]
                 (syntax
                   (if (= true (first x))
                     [:v-box
                      [:text "Correct! 🎉"]
                      [:cmd
                       {:name "Next"}
                       (str '(tutorial-2))]]
                     [:text
                      "Oops.. try again."])
                   {:interact? true})))}

           "(= x true)"]

          back-to-menu]

         {:interact? true
          :cls? true}))

     (defn tutorial-2 []
       (syntax
         [:v-box
          [:md "### Problem 2"]

          [:md "Complete the expression so it will evaluate to true."]

          ;; Command to check the solution.
          [:cmd
           {:name "Check"
            :mode :query
            :show-source? true
            :input [[:x]]
            :lang
            (str
              '(fn [form & [x]]
                 (syntax
                   (if (= 4 (first x))
                     [:v-box
                      [:text "Correct! 🎉"]
                      [:cmd
                       {:name "Next"}
                       (str '(tutorial-3))]]
                     [:text
                      "Oops.. try again."])
                   {:interact? true})))}

           "(= x (- 10 (* 2 3)))"]

          back-to-menu]

         {:interact? true
          :cls? true}))

     (defn tutorial-3 []
       (syntax
         [:v-box
          [:md "### Problem 3"]

          [:md "Lists can be constructed with either a function or a quoted form."]

          ;; Command to check the solution.
          [:cmd
           {:name "Check"
            :mode :query
            :show-source? true
            :input [[:x]]
            :lang
            (str
              '(fn [form & [x]]
                 (syntax
                   (if (= '(:a :b :c) (apply list x))
                     [:v-box
                      [:text "Correct! 🎉"]

                      [:cmd
                       {:name "Next"}
                       (str '(tutorial-4))]]

                     [:v-box
                      [:text
                       "Oops.. try again."]

                      [:code (str `(not (= (:a :b :c) ~(apply list x))))]])
                   {:interact? true})))}

           "(= (list x) '(:a :b :c))"]

          back-to-menu]

         {:interact? true
          :cls? true}))

     (defn tutorial-4 []
       (syntax
         [:v-box
          [:md "### Problem 4"]

          [:md "When operating on a list, the conj function will return a new list with one or more items \"added\" to the front."]

          ;; Command to check the solution.
          [:cmd
           {:name "Check"
            :mode :query
            :show-source? true
            :input [[:x]]
            :lang
            (str
              '(fn [form & [x]]
                 (syntax
                   (if (= '(1 2 3 4) (first x))
                     [:v-box
                      [:text "Correct! 🎉"]

                      [:cmd
                       {:name "Next"}
                       (str '(problem5))]]

                     [:text
                      "Oops.. try again."])
                   {:interact? true})))}

           "(= x (conj '(2 3 4) 1))\n(= x (conj '(3 4) 2 1))"]

          back-to-menu]

         {:interact? true
          :cls? true}))
     )


  ;; GUI to transfer coins

  '(syntax
     [:cmd
      {:name "Transfer"

       :input
       [["Address" {}]
        ["Amount" {}]]

       :lang
       (str '(fn [_ & [receiver amount]]
               (transfer (address receiver) amount)

               (syntax
                 [:v-box
                  [:p
                   [:text "Successfully transfered "]
                   [:text amount]
                   [:text " to "]
                   [:text (str (address receiver))]
                   [:text "."]]

                  [:h-box
                   [:text "Your balance is:"]
                   [:text *balance*]]]
                 {:interact? true})))}
      "(silly-fn nil)"]
     {:interact? true})


  )