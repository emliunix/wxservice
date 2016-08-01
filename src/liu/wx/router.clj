(ns liu.wx.router)

;; route format
;; event handler

(defn- get-msg-type [wx-data]
  (->
   (let [msg-type (:MsgType wx-data)]
     (if (= "event" msg-type)
       (let [event-type (:Event wx-data)]
         (if (and
              (= "subscribe" event-type)
              (not (nil? (:EventKey wx-data))))
           ;; 是一个subscribe-and-scan事件
           :subscribe-and-scan
           ;; 否则直接返回event-type
           event-type))
       msg-type))
   ;; 检查结果是不是nil，如果是nil，转换为:invalid
   ;; 否则传入keyword保证返回的是一个keyword
   ((fn [msg-type]
      (if (nil? msg-type)
        :invalid
        (keyword msg-type))))))

(defn router [handlers]
  (fn [req]
    (let [wx-data (:wx-data req)
          event-type (get-msg-type wx-data)
          handler (event-type handlers)
          handler (if (nil? handler)
                    (:default handlers)
                    handler)]
      (if-not (nil? handler)
        (handler req)
        {:status 400
         :body (str "No handler found for event-type: " event-type)}))))
