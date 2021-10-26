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
       :show-source? true
       :lang (str '(fn [x y] [x y]))}
      (str '(inc 1))]
     {:interact? true
      :cls? true})

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? true
       :lang (str '(fn [x y] (eval x)))}
      (str '(inc 1))]
     {:interact? true
      :cls? true})

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? true
       :input {:x {:type "number"}
               :y {:type "number"}}
       :lang (str '(fn [_ input]
                     (let [n (max
                               (get input :x)
                               (get input :y))]
                       (syntax
                         [:h-box
                          [:text "Max is:"]
                          [:code n]]
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


  ;; 4Clojure
  ;; -- https://4clojure.oxal.org/#/problem/1

  '(defn problem1 []
     (syntax
       [:v-box
        [:md "### Problem 1"]

        [:md "Complete the expression so it will evaluate to true."]

        ;; Problem code snippet.
        [:code "(= _ true)"]

        ;; Command to check the solution.
        [:cmd
         {:name "Check"
          :mode :query
          :show-source? true

          ;; `lang` allow us to manipulate a form before sending to the server.
          :lang
          (str
            '(fn [x _]
               (syntax
                 (if (= true (eval x))
                   [:v-box
                    [:text "Correct! 🎉"]
                    [:cmd
                     {:name "Next"}
                     (str '(problem2))]]
                   [:text
                    "Oops.. try again."])
                 {:interact? true})))}

         ";; Type your solution here ✍️"]]

       {:interact? true
        :cls? true}))

  '(defn problem2 []
     (syntax
       [:v-box
        [:md "### Problem 2"]

        [:md "TODO"]]

       {:interact? true
        :cls? true}))


  ;; Transfer

  '(syntax
     [:cmd
      {:name "Transfer"

       :input
       {"Amount" {:type "number"}
        "Address" {:type "number"}}

       :lang (str '(fn [form input]
                     (let [receiver (address (get input "Address"))
                           amount (get input "Amount")]

                       (transfer receiver amount)

                       (syntax
                         [:v-box
                          [:p
                           [:text "Successfully transfered "]
                           [:text amount]
                           [:text " to "]
                           [:text receiver]
                           [:text "."]]

                          [:h-box
                           [:text "Your balance is:"]
                           [:text *balance*]]]
                         {:interact? true}))))}
      "nil"]
     {:interact? true})



  )