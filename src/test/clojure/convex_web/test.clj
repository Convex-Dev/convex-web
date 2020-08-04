(ns convex-web.test)

(defmacro with-try [& body]
  `(try
     ~@body
     (catch Exception ex#
       (.printStackTrace ex#))))
