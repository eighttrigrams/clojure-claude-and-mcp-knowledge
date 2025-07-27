(ns tools
  (:require [clojure.edn :as edn]
            [et.vp.ds.search :as search]
            [et.vp.ds :as ds]
            [clojure.pprint :as pprint]
            tools
            [cheshire.core :as json]))

(def env :prod)

(def db (:db (edn/read-string (slurp 
                               (str (if (= :prod env)
                                      "/Users/daniel/Applications"
                                      "..")
                                    "/tracker/config.edn")))))

(def tools-list 
  {:get-contexts
   {:name "get_contexts"
    :description 
    "Tracker is a superhuman memory system which here represent's my (Daniel, the User) memory.
     Instead of sending long search strings to this MCP, often one finds things by looking
     them up in the right category, which in Tracker is called \"Context\".

     Everything is an \"Item\" in Tracker. It is the basic unit. 
     And the \"Contexts\" are a subset of all the \"Items\" stored in Tracker.
     \"Contexts\" may denote subjects, topics, collections, categories, or simply items that group a lot of other items.

     Searches in Tracker are done, instead of sending long search strings, rather by finding items in the right contexts.
     This is why every search in Tracker should start with breaking down the thing we search into their likely categories (contexts)
     they might be found in, and then find related items.

     \"Contexts\" tend to have shorter titles, non context \"Items\" tend to have longer, 
     more specific titles (who's titles are unlikely to be matched by any particular search terms \"q\")."
    :inputSchema
    {:type       "object"
     :properties
     {:q             
      {:type        "string"
       :minLength 3
       :maxLength 15
       :description "<IMPORTANT>With more than one search term, usually prefer two queries to the tool with each word over one call with a \"q\" comprised of two words.
                     Example. Rather than \"books reading\" issue one call with \"q\" as \"books\", and if that yields nothing, try \"q\" with \"reading\". Keep it short
                     and break search terms down.</IMPORTANT>
                     
                     Query string. Obviously, when trying to find anything, we need to narrow down the search result, ideally such that 
                     the thing we search for is the top search result. Note that normally we limit the results to 10, so you might not even see any more."}}
     :required   ["q"]}}
   :get-items
   {:name        "get_items"
    :description 
    "@tool get_items
     
     <SEARCH STRATEGY>
     <DO>
     Before calling this tool, especially when you are about to call it with longer search terms, consider calling the \"get_contexts\" tool first.
     </DO>

     <DO>
     Search in Tracker system usually involve multiple tool calls to various tools
     to finally arrive at good search results. The first step in such a search-chain is
     always via get_items, and specifically the first step is always to find a couple
     of suitable contexts via get_contexts.

     In general search results in tracker yield items (which may or may not be considered context items, see property is_context),
     but without description. So at any point, if you have an item in tracker,
     to fetch its description to get more insight to inform further searches, 
     use the get_item tool, to get the item with its description.

     When searching for specific persons you know the name of, try using the get_people tool.
     </DO>
     <DONT>
     Instead of for example searching for \"World War II videos\", in Tracker,
     always search for \"World War II\" and \"Videos\" contexts. Longer chains
     of words in searches are indicators that the purpose of Tracker as an intersection
     search based search system is defeated, or its potential not fully used during search.
     
     Don't use this reflexively to search for people you know names of. For that, rather use the get_people tool.
     </DONT>
     </SEARCH STRATEGY>

     Result lists are in order of most recently touched first (where touched can mean a 
     modification or just having looked at it). This is designed such on purpose, because
     Tracker represeents my personal memory and more recently touched items are literally
     \"on the top of my mind\". Note that this also means that items first listed 
     should be understood to have a higher weight/relevance.

     Sometimes searches for specific search terms (via the q argument) and without
     the only_contexts argument make sense, but instead of lets say searching 
     Quotes for the topic of World War II, in Tracker the recommended strategy is
     searching for a Quotes context and then a World War II context,
     and then continuing with intersection searches with the get_related_items tool.
     
     One other important thing, while context items are meant to act as containers
     and entry point for searches, EVERY item has the same property that you
     can get its description via get_item, and find items related to it via
     get_related_items."
    :inputSchema {:type       "object"
                  :properties {:q {:type        "string"
                                   :minLength 3
                                   :description "Query string. Obviously, when trying to find anything, we need to narrow down the search result, ideally such that 
                                          the thing we search for is the top search result. Note that normally we limit the results to 10, so you might not even see any more."}}
                  :required   ["q"]}}
   :get-people
   {:name "get_people"
    :description "Best way to find specific persons in Tracker
                  Whenever you know the name of a person and want to know mere
                  look here first!!!"
    :inputSchema 
    {:type       "object"
     :properties {:q {:type        "string"
                      :minLength   2
                      :description "Query string to find a specific person, usually put the name or part of the name of the person you are searching for here."}}
     :required   ["q"]}}
   :get-related-items
   {:name        "get_related_items"
    :description "Asks Tracker about related items to a given context item and lists 
                  them in the order
                  most recently touched first (where touched can mean a 
                  modification or just having looked at it).
                  
                  <SEARCH STRATEGY>

                  <DO>
                  Whenever you can, **prefer** intersection search using the secondary_contexts_items_ids
                  argument, thereby narrowing down results with multiple context ids, over using
                  all to specific search strings provided under the q parameter.
                  
                  If you can get away with it, then use multiple ids in secondary_contexts_items_ids
                  to make the search even narrower defined. 
                  
                  When doing that, make the broader categories/contexts ids be provided
                  in secondary_contexts_items_ids and the less specific id provided as
                  selected-context-item-id. For example, Quotes on the Second World War ->
                  Quotes is the broader category, so the quotes id is provided via
                  secondary_contexts_items_ids and the Second World War id is provided via
                  selected_context_item_id.

                  When searching for specific persons you know the name of, try using the get_people tool.
                  </DO>
                  <DONT>
                  Avoid doing searches without at least trying intersections
                  using secondary_contexts_items_ids argument.
                  </DONT>
                  </SEARCH STRATEGY>
                  "
    :inputSchema {:type       "object"
                  :properties {:q {:type        "string"
                                   :description "Query string. 
                                                 
                                                 <STRATEGY>
                                                 <DONT>
                                                 Don't use this argument reflexively. Instead try
                                                 provide additional ids via secondary_contexts_items_ids
                                                 </DONT>
                                                 <DO>
                                                 Before using this argument, always
                                                 try to make full use of supplying additional
                                                 contexts via secondary_contexts_items_ids.
                                                 Only when you repeatedly hit no results,
                                                 supply a query string.
                                                 </DO>
                                                 </STRATEGY>

                                                 Before I describe this argument, note that it is often preferential to
                                                 pre-filter items by using intersection search by passing secondary_contexts_items_ids
                                                 rather than using an actual search term for q.
                                                 
                                                 Obviously, when trying to find anything, we need to narrow down the search result, ideally such that 
                                          the thing we search for is the top search result.
                                                 
                                          However, when a selected_context is given, you might want to give an empty query string to see all items available and related to a given context.
                                                 
                                                 An even better strategy within Tracker to filter for good results is to specify secondary-contexts-items-ids to 
                                                 search in intersections of contexts. This is often better to use query strings."}
                               :selected_context_item_id 
                               {:type        "string"
                                :description "an id number to narrow down the search results to items related to that context.
                                                when doing intersection searches, use this id for the narrowest/most specific of the contexts."}
                               :secondary_contexts_items_ids
                               {:type "array"
                                :items {:type "string"}
                                :description "A definitive success strategy in Tracker:
                                              When doing a get_related_items query that is narrowed down
                                              by additional contexts, only items are shown which are also part of these other contexts.
                                              
                                              When doing intersection searches, make broader and more generic categories  
                                              be deployed via secondary_contexts_items_ids and reserve selected-context-item-id 
                                              for the narrowest of the contexts."}}
                  :required   ["q" "selected_context_item_id"
                               "secondary_contexts_items_ids"]}}
   :get-item-with-description-and-related-items
   {:name "get_item_with_description_and_related_items"
    :description 
    "When we are dealing with items which are not contexts (i.e. `is_context` is false)
     these are what one could consider leaf nodes which have not the character
     of being a subject, a topic, a collection, a category, or a container.
     
     In Tracker, relations have no clearly defined semantics per se, so by design we do not precisely
     define why items are related to another item. In the case of these leaf character items,
     which often have longer titles (IMPORTANT!) than the other mentioned types of items like subjects,
     topics, collections, categories, or containers (which tend to have shorter titles),
     when we fetch them, we excpect them to have at most a few related items. In this
     case we are interested in getting their description as well as these related items
     at once and prefer that (IMPORTANT!) definitely for those types of items over calls to 
     the \"get_item\" tool.
     "
    :inputSchema {:type       "object"
                  :properties {:id {:type        "string"
                                    :description "the item's id, as the issues returned from get_items and get_related_items always include."}}
                  :required   ["id"]}}
   :get-item 
   {:name        "get_item"
    :description 
    "One thing upfront. For items which are not of context character 
     (like topics, subjects, containers, collections, resources), prefer the 
     \"get_item_with_description_and_related_items\" over calls to \"get_item\" 
     plus \"get_related_items\".

     Asks Tracker about a single item, and that means, in contrast
     to listing items with for example get_issues, that the description is included,
     Which is what we are often also interested in, once we identified the specific 
     item we are after.
                              
     Also, once you have an issue that looks interesting, you should definitely
     more often than not also check for its related item, ussing its id,
     using get_related_items and the selected_context_item_id parameter.
                              "
    :inputSchema {:type       "object"
                  :properties {:id {:type        "string"
                                    :description "the item's id, as the issues returned from get_items and get_related_items always include."}}
                  :required   ["id"]}}})

(defn get-contexts [{:keys [q] :as _arguments}]
  #_(when selected_context_item_id (throw (IllegalArgumentException. "shouldn't pass selected_context_item_id. For this use get-related-items")))
  #_(when (not (or (= "true" only_contexts) (nil? only_contexts))) 
    (throw (IllegalArgumentException. "only contexts should either be \"true\" or nil/null (omit the parameter/argument entirely when it should say anything other than true)")))
  (search/search-items db q {} {:limit 10}))

(defn get-items [{:keys [q selected_context_item_id _only_contexts] :as _arguments}]
  (when selected_context_item_id (throw (IllegalArgumentException. "shouldn't pass selected_context_item_id. For this use get-related-items"))) ;; ??
  #_(when (not (or (= "true" only_contexts) (nil? only_contexts))) 
    (throw (IllegalArgumentException. "only contexts should either be \"true\" or nil/null (omit the parameter/argument entirely when it should say anything other than true)")))
  (search/search-items db q {:all-items? true} {:limit 10}))

(defn get-people [{:keys [q] :as _arguments}]
  (search/search-related-items db 
                               q 
                               10960
                               {}
                               {:limit 10}))

(defn get-related-items [{:keys [q selected_context_item_id secondary_contexts_items_ids] :as _arguments}]
  (search/search-related-items 
   db
   q 
   selected_context_item_id
   {:selected-secondary-contexts secondary_contexts_items_ids}
   {:limit 10}))

(defn trim-to [n text]
  (if (> (count text) n)
    (subs text 0 n)
    text))

(defn get-item [{:keys [id] :as _arguments}]
  (ds/get-item db 
               ;; TODO make the & arg-map thing to pass in :id id without specifying map, then check whether arg is id, or title, to replace get-by-title
               {:id id}))

(defn get-item-with-description-and-related-items [{:keys [id] :as _arguments}]
  (let [item (ds/get-item db {:id id})]
    (when (:is_context item) (throw (UnsupportedOperationException. "Call this only for non is_context items.")))
    {:item-with-description item 
     :related-items (search/search-related-items db "" (:id item) {:selected-secondary-contexts []} {})}))

(defn map-tool [name]
  (case name
    "get_contexts" get-contexts
    "get_items" get-items
    "get_people" get-people
    "get_related_items" get-related-items
    "get_item" get-item
    "get_item_with_description_and_related_items" get-item-with-description-and-related-items
    nil))

(comment
  (require '[cheshire.core :as json])
  (json/generate-string (get-items {:q "YouTube"}))
  (pprint/pprint (get-items {:q "YouTube"}))
  
  (pprint/pprint (trim-to 500 (:description (get-item {:id 34696}))))
  )
