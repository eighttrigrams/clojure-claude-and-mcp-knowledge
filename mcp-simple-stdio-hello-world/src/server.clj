(ns server
  (:require [cheshire.core :as json] 
            tools))

(defn handle-request [request]
  (let [method (:method request)
        params (:params request)]
    (merge 
     {:jsonrpc "2.0"}
     (cond
       (= method "initialize")
       {:result {:protocolVersion "2024-11-05"
                 :capabilities    {:tools {}}  ;; Just declare "we have tools"
                 :serverInfo      {:name    "tracker-mcp"
                                   :version "1.0.0"}}}
       (= method "tools/list")
       {:result  {:tools (mapv second tools/tools-list)}}
       (= method "tools/call")
       (let [tool-name (get-in params [:name])
             arguments (get-in params [:arguments])
             f (tools/map-tool tool-name)]
         (if f 
           (let [result (try (f arguments)
                             (catch IllegalArgumentException e
                               {:error {:code    -32602
                                        :message (.getMessage e)}})
                             (catch UnsupportedOperationException e
                               {:error {:code    -32801
                                        :message (.getMessage e)}}))]
             {:result  {:content [{:type "text"
                                   :text (json/generate-string result)}]}}) 
           {:error   {:code    -32601
                      :message "Unknown tool"}}))
       :else
       {:error   {:code    -32601
                  :message "Unknown method"}}))))

(defn process-line [line]
  (when-not (empty? line)
    (try
      (let [request (json/parse-string line true)
            response (handle-request request)
            response-with-id (assoc response :id (:id request))]
        (println (json/generate-string response-with-id))
        (flush))
      (catch Exception _e
        (println (json/generate-string {:jsonrpc "2.0"
                                        :error {:code -32700
                                                :message "Parse error"}
                                        :id nil}))
        (flush)))))

(defn -main [& _args]
  (doseq [line (line-seq (java.io.BufferedReader. *in*))]
    (process-line line)))
