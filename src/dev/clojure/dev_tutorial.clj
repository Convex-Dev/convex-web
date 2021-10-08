(ns dev-tutorial)

(comment

  '(syntax
     [:text "Text explaining something in the Sandbox."]
     {:interactive? true})

  '(syntax
     [:caption "Tiny text for caption."]
     {:interactive? true})

  '(syntax
     [:button
      {:action :edit
       :source "(inc 1)"}
      "Edit action"]
     {:interactive? true})

  '(syntax
     [:button
      {:action :query
       :source "(inc 1)"}
      "Query action"]
     {:interactive? true})

  '(syntax
     [:markdown "# Title\n\n## Subtitle"]
     {:interactive? true})

  '(syntax
     [:v-box
      [:markdown
       "### Convex Lisp Tutorial\n\nLet's define a function `f`:"]

      [:h-box

       [:code
        "(defn f [x] x)"]

       [:button
        {:action :edit
         :source (str '(defn f [x] x))}
        "Define a function"]]

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
               "Call a function"]]

             {:interactive? true}))}
       "Continue"]]

     {:interactive? true})

  )