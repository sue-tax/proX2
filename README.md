# proX2
javaの実行環境が必要です。
https://www.oracle.com/jp/java/technologies/downloads/

javaFXのライブラリが必要です。
https://gluonhq.com/products/javafx/

Windows 19 x64 SDK をダウンロードし、解凍します。


javaFXのライブラリを指定するために、
proX.bat で起動します。
proX.bat内のmodule-pathは、上記のjavaFXライブラリの解凍先に書き換えます。

## 概要
テキストデータ（マークダウン記法を推奨）の中の変数を含めた計算式を計算します。

例えば、次のようなテキストデータを変換すると、
```（元テキスト）
"配当:5,000円"
”国税：'配当'×0.15×1.021＝|d0|"
手取[’配当’－’国税’]
```
下記のように、変換されます。
```（変更後テキスト）
"配当:5,000円"
”国税：'配当:5,000円'×0.15×1.021＝765円|d0|"
手取[’配当:5,000円’－’国税:765円’=4,235円]
```
変数の定義は、" " で囲みます。: の前が変数名、: の後ろが変数の値です。

変数の参照は、' ' で変数名を囲みます。

|d0| は、端数処理の指示で、小数点以下第０位より下の桁を切捨てます。


[ ] は、計算をするだけです。

[表](#表変数)を利用した簡単な計算もできます。

数値には、全角や漢数字を使えます。
記号も全角が使えます。

## 想定する利用方法

テキストデータにより作業メモなどを作成している方にお勧めです。
Excelを使うほどではない計算をテキストデータに埋め込むことができます。
お客さん毎に金額が変わる場合、毎年金額が変わる場合などに、便利に利用できます。

テキストファイルを読込み、変換後、テキストファイルを保存する。
翌年は、そのテキストファイルを変わった金額だけ変更し、変換をする。

## 表変数

マークダウンの表を「"変数名:{」と「}"」で囲むと、表を利用した変数になります。
「'変数名'(行指定)(列指定)」で表変数を参照できます。
行・列は一番上、一番左が1です。0は、一番下の行、一番右の列を指定します。
数による指定のほかに、値指定・オフセット指定が可能です。
詳しくは、「proX構文マニュアル.md」を参照してください。

```（元テキスト）
"関係会社利息:1,000,000円"

"預金利息:{

| 銀行名等     | 総額                | 国税                           | 手取                       |
| ------------ | ------------------- | ------------------------------ | -------------------------- |
| 三井住友普通 | 100                 | '.'(@0)(@-1)×0.15×1.021=｜d0｜ | '.'(@0)(@-2)-'.'(@0)(@-1)= |
| 定期         | 1000                | '.'(@0)(@-1)×0.15×1.021=｜d0｜ | '.'(@0)(@-2)-'.'(@0)(@-1)= |
| 合計         | `&sum('.'(*)(@0));` | `&sum('.'(*)(@0));`            | `&sum('.'(*)(@0));`        |

}"

”受取利息：’関係会社利息’＋’預金利息’（０）（２）＝”
['預金利息:'(2)(手取）]
```

```（変更後テキスト）
"関係会社利息:1,000,000円"

"預金利息:{

| 銀行名等      | 総額                 | 国税                            | 手取                        |
| ------------ | ------------------- | ------------------------------ | -------------------------- |
| 三井住友普通  | 100                  | '.'(@0)(@-1)×0.15×1.021=15｜d0｜  | '.'(@0)(@-2)-'.'(@0)(@-1)=85  |
| 定期          | 1000                 | '.'(@0)(@-1)×0.15×1.021=153｜d0｜  | '.'(@0)(@-2)-'.'(@0)(@-1)=847  |
| 合計          | `&sum('.'(*)(@0));1100`  | `&sum('.'(*)(@0));168`             | `&sum('.'(*)(@0));932`         |

}"

”受取利息：’関係会社利息:1,000,000円’＋’預金利息’（０）（２）＝1,001,100円”
['預金利息:85'(2)(手取）=85]
```



