(ns convex-web.test)

(defmacro catchy [body]
  `(try
     ~body
     (catch Exception ex#
       (.printStackTrace ex#))))
