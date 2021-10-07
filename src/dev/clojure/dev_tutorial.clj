(ns dev-tutorial)

(comment

  '(syntax
     [:v-box
      [:text "Let's define a function `f`:"]

      [:h-box

       [:code
        "(defn f [x] x)"]

       [:button
        {:action :edit
         :source (str '(defn f [x] x))}
        "Try yourself"]]

      [:caption
       "Click on 'Continue' once you have executed the step above."]

      [:button
       {:action :query
        :source
        (str
          '(syntax
             [:v-box
              [:text "Now let's call `f`, and pass `1` as argument:"]

              [:button
               {:action :edit
                :source (str '(f 1))}
               "Try yourself"]]

             {:interactive? true}))}
       "Continue"]]

     {:interactive? true})

  )