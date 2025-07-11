# weather

## Descrição

Projeto de estudo em ClojureScript.

## Comando para gerar o template deste projeto:
```bash
clj -X:new :template figwheel-main :name learn-cljs/weather :args '["+deps" "--reagent"]'
```

- 'clj -X:new' -> Invoca a função clj-new/create;
- :template    -> Especifica o nome do modelo a ser usado; 
- :name        -> Nome do projeto a ser criado;
- :args        -> Argumentos adicionais que o modelo figwheel-main interpretará.

## Comando para iniciar o Figwheel:
```bash
clj -M:fig:build
```

## Os pilares do código recarregável

- Funções idempotentes;
- `defonce`;
- Segregação de lógica de negócios/exibição.

### Funções idempotentes
Uma função idempotente é uma função que terá o mesmo efeito se for chamada uma ou várias vezes.
Por exemplo, uma função que define a propriedade innerHTML de um elemento DOM é idempotente,
mas uma função que anexa um filho a algum outro elemento não é:
```clojure
(defn append-element [parent child] ;; <1>
  (.appendChild parent child))

(defn set-content [elem content] ;; <2>
  (set! (.-innerHTML elem) content))
```
1. Função não idempotente
2. Função idempotente

### `defonce`
Quando criamos um projeto Figwheel com o argumento --reagent, o namespace que ele gera
usa uma construção chamada defonce para definir o estado do aplicativo:
```clojure
(defonce app-state (atom {:text "Hello world!"}))
```
Como mencionamos acima, o defonce é muito semelhante ao def, mas, como o nome sugere,
ele vincula a variável apenas uma vez, ignorando efetivamente a expressão nas avaliações subsequentes.
Geralmente, definimos o estado do nosso aplicativo com defonce para que ele não seja substituído por
um novo valor sempre que o código for recarregado.
Dessa forma, podemos preservar o estado do aplicativo junto com quaisquer dados transitórios
enquanto a lógica comercial do nosso aplicativo é recarregada.
Outro padrão útil para o uso de defonce é proteger o código de inicialização da execução repetida.
Uma expressão de defonce tem a forma: (defonce name expr) em que name é um símbolo que nomeia a variável
a ser vinculada e expr é qualquer expressão ClojureScript.

### Segregação de lógica de negócios/exibição.
A separação do código de exibição e da lógica comercial é uma boa prática em geral,
mas é ainda mais importante para o código recarregável.
Errado:
```clojure
(defn receive-message [text timestamp]
  (let [node (.createElement js/document "div")]
    (set! (.- innerHTML node) (str "[" timestamp "]: " text))
    (.appendChild messages-feed node)))
```
Certo:
```clojure
(defonce messages (atom []))                               ;; <1>

(defn receive-message [text timestamp]                     ;; <2>
  (swap! messages conj {:text text :timestamp timestamp}))

(defn render-all-messages! [messages]                      ;; <3>
  (set! (.- innerHTML messages-feed) "")
  (doseq [message @messages]
    (let [node (.createElement js/document "div")]
      (set! (.-innerHTML node) (str "[" timestamp "]: " text))
      (.appendChild messages-feed node))))

(render-all-messages!)                                     ;; <4>
```
1. Todas as mensagens recebidas são armazenadas em um átomo definido que não será sobrescrito;
2. A função que lida com novas mensagens é pura lógica de negócios;
3. A função que renderiza as mensagens é pura lógica de exibição;
4. Executar uma renderização.



