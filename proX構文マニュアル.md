# proX,proTex 構文マニュアル

<div style="text-align:right">Ver.0.8.3</div>

　proXはテキストの中に指定された式を計算し、計算結果をテキストに書き込むソフトです。

　proXで扱うテキストの文字コードは、==utf-8==です。ANSIだと文字化けします。

　式や変数などの計算に関係ないテキストは、読み飛ばして処理します。

　マークダウン記法[^1]を考慮して文法を定めました。特に、Typora[^2]というマークダウンエディタとの相性が良いです。

　なお、構文中の半角の記号の代わりに、全角の記号も使えます。

[^1]:https://www.markdown.jp/syntax/
[^2]:https://typora.io/

　proXの使用方法等は、[proX簡易マニュアル](proX簡易マニュアル.md)を参照してください。

[TOC]

## 文法

##### 省略可能

　【 】内は、省略可能です。

###### 省略可能だが重要な機能
　省略可能な中には重要な機能もあります。

　[端数処理指示](#端数処理指示)は、使う機会が多いと思います。

　[変数パス指定](#変数パス指定)は、変数の指定方法を工夫できるので、利用すべき機能と思います。

　[登録タイミング指定](#登録タイミング指定)は、現バージョンでは不要です。

###### 省略可能で必要性が低い機能（デバッグ機能）

　「?」で始まるものは必要性が低い機能です。いわゆるデバッグのための機能です。
　変数の指定が思い通りにできなかったときや、計算結果が想定と異なるときに、原因を調べるために利用する機能です。

### 変数定義

　変数に、値を設定します。

#### 通常変数定義

```
"変数名【&登録タイミング指定】【?変数パス表示】:計算式【計算結果指示】【?演算式表示】【端数処理指示】"
```

　[計算式](#計算式)を計算し、[変数](#変数)に計算結果が定義されます。

　[端数処理指示](#端数処理指示)があれば、小数点以下３位未満切捨てなどの端数処理した値が計算結果になります。

```
"基礎控除額:1,100,000円"
　　基礎控除額という名前の変数に「1,100,000円」が定義される
```

##### 登録タイミング指定

　システム内部の処理方法を変更したため、登録タイミングの指定は不要となりました。
　以前に作成したテキストデータでエラーが発生しないように、文法上は残してあります。

　~~変数を定義するための計算式の中で、他の変数を参照することができます。
　変数を定義するのは、原則、先頭から順番に行います。登録タイミング指定を行うと、定義の順番を変更することができます。
　変数「評価額」を定義するために計算するときには、まだ変数「路線価」、「地積」が定義されていないので、エラー（!変数が見つからない!）となります。
　下記のように「評価額&2」とすると、先に「路線価」と「地積」を定義するので、「評価額」を定義するための計算が可能になります。
　変数タイミング指定がない変数は、1が指定されたとして処理します。~~

##### 変数パス表示

==未作成==

[変数の範囲指定](#変数の範囲指定)を参照

```
《元テキスト》
# 所得税
## 不動産所得
"総収入金額?:3,600,000円"
《変換後テキスト》
# 所得税
## 不動産所得
"総収入金額?/root/所得税/不動産所得:3,600,000円"
　　「/root/所得税/不動産所得」が変数パスになります。
```

##### 計算結果指示

　等号（半角の「=」、全角の「＝」　等号の後に半角空白、全角空白１つだけ可能）があれば、計算結果でテキストを書き換えます。

　計算結果指示がなくても、計算結果は変数に定義されます。

```
《元テキスト》
"計算結果を見たい:1+2 = 4"
"復興税込みの税率:20%×1.021"
《変換後テキスト》
"計算結果を見たい:1+2 = 3"
　　等号の後の「4」を計算結果「3」に書き換える
"復興税込みの税率:20%×1.021"
　　計算結果は表示されないが、変数「復興税込みの税率」は0.2042
```

##### 演算式表示

 ==未作成==

```
《元テキスト》
"総収入金額:3,600,000円"
"必要経費:1,200,000円"
"所得金額:'総収入金額'-'必要経費'=?"
《変換後テキスト》
"総収入金額:3,600,000円"
"必要経費:1,200,000円"
"所得金額:'総収入金額:3,600,000円'-'必要経費:1,200,000円'=2,400,000円?(3,600,000円)-(1,200,000円)"
　　「(3,600,000円)-(1,200,000円)」が式表示の式になります。
```

##### 無名変数

　通常は同じ名前の変数が同じキャプション内で重複すると、エラーになります。

　「_」（アンダースコア）という名前の変数だけは重複を許します。
　下記のような使用方法を想定しています。

```
《元テキスト》
### リサイクル預託金
"リサイクル預託金:`&sum('*')`="
クラウン "_:20,000円"
ハイエース "_:15,000円"
ハイエース "_:16,000円"
軽トラ "_:9,000円"
ハイエース "_:14,000円"
《変換後テキスト》　ハイエースを変数名にすると重複エラーになる
### リサイクル預託金
"リサイクル預託金:`&sum('*')`=74,000円"
クラウン "_:20,000円"
ハイエース "_:15,000円"
ハイエース "_:16,000円"
軽トラ "_:9,000円"
ハイエース "_:14,000円"
```

　なお、マークダウンエディタTyporaを使用している場合は、
「"_:15,000円"」のアンダースコアが表示されず「":15,000円"」のように見えます。

#### 表変数定義

 ==未作成==

　マークダウン記法での表の前後を、```"変数名:{``` と ```}```で囲みます。

```
《元テキスト》
"相続人:{

| 相続人名 | 続柄 | 法定相続分   |
| -------- | ---- | ------------ |
| トメ     | 妻   | 1/2          |
| 太郎     | 長男 | 1/4          |
| 花子     | 長女 | 1/4          |

}"

'相続人:'(2)(3)
'相続人:'(花子)(続柄)
《変換後テキスト》
"相続人:{

| 相続人名 | 続柄 | 法定相続分   |
| -------- | ---- | ------------ |
| トメ     | 妻   | 1/2          |
| 太郎     | 長男 | 1/4          |
| 花子     | 長女 | 1/4          |

}"

'相続人:0.5'(2)(3)
'相続人:長女'(花子)(続柄)
```

　表変数参照は、行・列を指定します。

　行は上から１，２，３…と数えますが、横棒の行は数に入れません。

　各列の１行目の値、各行の１列目の値で、行・列を指定することができます。

### 変数参照

　指定された変数を、変数定義で設定された値に置き換えます。

　変数定義の計算式、計算指定の計算式で使用します。

#### 通常変数参照

```'【#変数パス指定:】指定変数名【?変数参照パス表示】【:変数値【?変数演算式】】'```

==未作成==

```
# 元テキスト
"再差引所得税額:90,000円"
"復興特別所得税額:'再差引所得税額'×2.1%＝ "
所得税及び復興特別所得税額　['再差引所得税額'＋'復興特別所得税額'＝]
# 変換後テキスト
"再差引所得税額:90,000円"
"復興特別所得税額:'再差引所得税額:90,000円'×2.1%＝ 1,890円"
所得税及び復興特別所得税額　['再差引所得税額:90,000円'＋'復興特別所得税額:1,890円'＝91,890円]
```

##### 変数パス指定

 ==未作成==

[変数の範囲指定](#変数の範囲指定)を参照

##### 変数参照パス表示

 ==未作成==

[変数の範囲指定](#変数の範囲指定)を参照

##### 変数値

　元テキストに変数値が記載されていても、変数に定義されていた値に書き変えられます。

##### 変数演算式

　変数定義の際の計算式（演算式）を表示します。

```
《元テキスト》
"受取利息:100+80="
'受取利息:?'
《変換後テキスト》
"受取利息:100+80=180"
'受取利息:180?100+80'
　　変数値は変数「受取利息」の値の「180」ですが、
　　変数演算式は変数定義の際に使った計算式「100+80」を表示します
```

#### 表変数参照

```'【#変数パス指定:】指定変数名【?変数参照パス表示】【:変数値【?変数演算式】】(行指定)(列指定)'```

　行指定・列指定は、数指定・値指定・オフセット指定ができます。
　数指定は、１以上の整数です。行は、タイトル行を含めて上から数えます（タイトル行が１です）。列は、左から数えます。0の場合は、一番下の行または一番右の列の指定になります。
　数指定は、変数参照を含めた計算式で指定することもできます。

　値指定は、タイトル行（一番上の行）・左端の列の文字列を指定します。

　オフセット指定は、先頭に@を付けた整数です。表の中で使います。

　[表変数定義](#表変数定義)を参照してください。

### 計算指定

　指定された計算式を計算し、計算結果を置き換えます。

```[計算式【計算結果指示】【?実際計算式】【端数処理指定】]```

　但し、```[集計表](集計表.xlsx)```のように後ろに「(」が続くものは、マークダウンにおいてリンクを表すので、無視します。

　同様に、`[^1]`、`[^1]:説明文`は、マークダウンにおいて注釈を表すので無視します。

 ==未作成==

```
《元テキスト》
"受取配当金:1,000"
['受取配当金'×（15%×1.021）|d0|]
《変換後テキスト》
"受取配当金:1,000"
['受取配当金:1,000'×（15%×1.021）=153|d0|]
```

#### 計算結果指示

　等号（半角の「=」、全角の「＝」　等号の後に半角空白、全角空白１つだけ可能）の後のテキストを計算結果で書き換えます。

　計算結果指示がなければ、等号（半角の「=」）と計算結果をテキストに追加します。

#### 実際計算式

 ==未作成==

### 見出し行

　マークダウン記法では、「#」の数で、見出しの大きさを変えることができます。

```
## 第２編_居住者の納税義務
### 第１章_通則
##### 第２１条_所得税額の計算の順序
　居住者の（以下、省略）
### 第２章_課税標準及びその計算並びに所得控除
#### 第１節_課税標準
##### 第２２条_課税標準
　居住者に対し（以下、省略）
#### 第２節_各種所得の金額の計算
```

　proXでは、見出し行をパソコンのフォルダのようなツリー構造で管理しています。

　見出しの大きさ（#の数）が飛んでいる場合は、ダミーの「_」があると考えます。

```
root
　└─_　　　　　　　　　　　　　　　　　　　　　　　　（#の分のダミー）
　　　└─第２編_居住者の納税義務
　　　　├─第1章_通則
　　　　│　└─_　　　　　　　　　　　　　　　　　　　　（####の分のダミー）
　　　　│　　　└─第21条_所得税額の計算の順序
　　　　└─第２章_課税標準及びその計算並びに所得控除
　　　　　　├─第１節_課税標準
　　　　　　│　└─第２２条_課税標準
　　　　　　└─第２節_各種所得の金額の計算
```

　このツリー構造は、[変数の範囲指定](#変数の範囲指定)を理解する上で、重要です。　

### 関数

　数値を計算する関数とproXのシステム的な関数があります。

　関数は、「`」で囲います。「'」とは異なります。

#### sum関数

``` `&sum('【#変数パス指定:】指定変数名【?変数参照パス表示】【:【変数値】【?【変数演算式】】】'【（行指定）（列指定）】　【, ...】)【;【計算額】【？【演算式】】】` ```

　指定された変数名に一致する全ての変数の合計を計算します。

　変数名に*を指定すると、自分自身の変数を除きます。

```
《元テキスト》
### 預り金
"預り金:`&sum('*')`="
"源泉税:156,200円"
"社会保険料:213,500円"
"住民税:98,000円"
《変更後テキスト》
### 預り金
"預り金:`&sum('*')`=467,700円"
"源泉税:156,200円"
"社会保険料:213,500円"
"住民税:98,000円"
```



　表変数の行指定・列指定は範囲を指定できます。「(開始行または列 : 終了行または列)」で指定します。「(*)」とした場合は２番目の行・列から最後から２番目の行・列までを指定します。

==未作成==

```
# 所得
## 配当
"源泉税:100"
## 株式譲渡
"源泉税:2000"
"源泉徴収税額:`&sum('源泉税:2,000')`=2,000"　　　株式譲渡の源泉税だけ合計
# 税額計算
"源泉徴収税額:`&sum('源泉税:100,2,000')`=2,100"　２つの源泉税を合計
```

#### count関数

　指定された変数名に一致する変数の数を計算します。

==未作成==

```
# 所得
## 配当
"源泉税:100"
## 株式譲渡
"源泉税:2,000"
"源泉徴収税額:`&count('源泉税:2,000')`=1"　　　株式譲渡の源泉税だけ
# 税額計算
"源泉徴収税額:`&count('源泉税:100:2,000')`=2"　２つの源泉税
"書き間違い:`&count('源泉徴収税:')`=0"
```

#### version関数

``` `&version【;【バージョン番号】【？バージョン表記文字列】】` ```

　proXのバージョン、proTexの場合は内部バージョンを表示します。

```
《元テキスト》
`&version`
《変換後テキスト》
`&version;0.82`
```

#### versionTex関数

``` `&versionTex【;【バージョン番号】【？バージョン表記文字列】】` ```

　proTexのバージョンを表示します。proXにはありません。

```
《元テキスト》
`&versionTex;?`
《変換後テキスト》
`&versionTex;0.04?TransString_0.90_proTex_0.04`
```

#### 

#### datetime関数

　proXが処理した日時を表示します。

　`&datetime_start`,`&datetime_end`,`&date`,`&time`も、あります。

```
《元テキスト》
`&datetime`
《変換後テキスト》
`&datetime;2022-12-29 16:10:54`
```

#### error関数

``` `&error【;【エラーの数】【？【エラーの数】】】` ```

　proXの処理で発生したエラーの数を表示します。

```
《元テキスト》
"a 3"
'a'
`&error`
《変換後テキスト》
`&error;3`
```

　一番最後の行に書くのが、一般的な使い方です。計算が終了したときに「変換後テキスト」は一番下を表示しますので、error関数、version関数、datetime関数を記載すると便利だと思います。
```
"法人税等:70,000"
---
`&version;proX version 0.7.1`
`&datetime;2022-11-10 12:05:11`
`&error;proX error 0`
```
　途中の行に書くと、その行までに発生したエラーの数を表示します。

　==プログラムのミスで、エラーをカウントしていなかったり、１つのエラーを２回、３回カウントしてしまうことがあります。==

　エラーが発生した場合は、「修正済みテキスト」に「!」で囲まれたエラーメッセージが書き込まれています。

#### assert関数

`&assert('【#変数パス指定:】指定変数名【?変数参照パス表示】:変数値【?変数演算式】】'【（行指定）（列指定）】,'【#変数パス指定:】指定変数名【?変数参照パス表示】:変数値【?変数演算式】】'【（行指定）（列指定）】【, ...】)【;結果【？計算額:計算額【: ...】】`

　指定された変数が全て同じ値かを確認します。全て同じ値ならば、0になります。同じ値でないものがあれば、エラーとなります。


```
《元テキスト》
"a:1234"
"b:1,234"
`&assert('a','b')`
"c:1,230+5="
`&assert('a','b','c');?`
"d:1234.3"
`&assert('a','d');?`
`&error`
《計算後テキスト》
"a:1234"
"b:1,234"
`&assert('a:1234','b:1,234');0`
"c:1,230+5=1,235"
`&assert('a:1234','b:1,234','c:1,235');!数が一致していません!?1234:1,234:1,235`
"d:1234.3"
`&assert('a:1234','d:1234.3');!数が一致していません!?1234:1234.3`
`&error;2`
```

#### スキップ指示関数

　計算式の中で、指示された文字で囲われた部分を無視します。

```
《元テキスト》
[三和銀行1000＋三井住友銀行2000]     三を数字として計算してしまう
`&proX_囲んだ文字をスキップ;{}`　　　 {}で囲んだ
[{三和銀行}1000＋{三井住友銀行}2000] 三和銀行、三井住友銀行を無視
《変換後テキスト》
[三和銀行1000＋三井住友銀行2000=三]     三を数字として計算してしまう
`&proX_囲んだ文字をスキップ;123125`　　　 {}で囲んだ
[{三和銀行}1000＋{三井住友銀行}2000=3000] 三和銀行、三井住友銀行を無視
```

#### 他の関数

　if関数などを検討していますが、未サポートです。

### TeX数式

　TeX数式のいくつかを計算式として使います。

　proTexのみの機能です。proXではサポートしていません。

```\frac{分子}{分母}【;【計算額】{}】```

```\sqrt【[累乗数]】{数}【;【計算額】{}】```

```\pi【;【計算額】{}】```

　ただし、`[$$\sqrt{xxx}$$]`などは可能ですが、`[$$ \sqrt[n]{xxx}$$]` などはエラーになります。

《元テキスト》

[$$\frac{2}{3}=$$]

ピタゴラスの定理
"a:3"
"b:4"
"c:$$\sqrt[2]{'a'^2 + 'b'^2}=$$"

[$$半径２^2 × \pi＝$$]

《変換後テキスト》

[$$\frac{2}{3}=0.66666$$]

ピタゴラスの定理
"a:3"
"b:4"
"c:$$\sqrt[2]{'a:3'^2 + 'b:4'^2}=5$$"

[$$半径２^2 × \pi＝１２.５６６３６$$]

### その他

#### 端数処理指示

```
|端数処理区分 端数処理桁|　または　|千円未満切捨て|　または　|百円未満切捨て|
及び、それぞれの前にn
```

==未作成==

　計算結果の端数の処理方法を指示します。

##### 端数処理区分

　udrのいずれか１文字です。

　それぞれ、切上げ(<u>u</u>p)、切捨て(<u>d</u>own)、四捨五入(<u>r</u>ound)を意味します。

##### 端数処理桁

　端数処理をする小数点以下の桁数です。０以上の数です。

```
《元テキスト》
[10,000÷3|d0|]
[10,000÷3|u1|]
[10,000÷3|r2|]
[10,000÷3|千円未満切捨|]
[10,000÷3|百円未満切捨|]
《変換後テキスト》
[10,000÷3=3,333|d0|]
[10,000÷3=3,333.4|u1|]
[10,000÷3=3,333.33|r2|]
[10,000÷3=3,000|千円未満切捨|]
[10,000÷3=3,300|百円未満切捨|]
```

##### 単位削除指定

　proXの計算では、「円」や「ｍ」などの単位を考慮して計算します。
　計算結果も単位を含めて表示します。
　計算結果の表示に単位が不要な場合は、端数処理指示に「n」を付けます。

```
《元テキスト》
[５年×12=]
[５年×12=|n|] 単位削除指定
《変換後テキスト》
[５年×12=６０年]
[５年×12=６０|n|] 単位削除指定
```

## 　名前

　変数名、見出し名には、半角英数字だけでなく、全角英数字、半角カタカナ、全角カタカナ、全角ひらがな、漢字が使えます。

　半角記号（アンダースコア「_」を除きます）、全角記号の大部分は使えません。

　漢字の中でも、㌢など特殊なものは使えません。

　次のような文字の置換えなどをしていますので、予期しない名前の重複が生じることがあります。例えば、「土地1」と「土地１」と「土地①」は、同じ変数名となります。

```
半角カタカナ　→　全角カタカナ
全角英数字　→　半角英数字
丸数字など（①⓶❸⑷など）　→　半角数字
㈱、㈲、㈹　→　株、有、代
最初の文字が数字　-> 最初にアンダースコアを追加
```



## 変数の範囲指定

　見出しで区切られた位置が違う場合は、同じ変数名を使うことができます。

```
# 配当所得
"国税:200円"　　別の位置に同じ変数名はOK
# 株式譲渡所得
"国税:3,400円"　別の位置に同じ変数名はOK
# 不動産譲渡
土地　"譲渡益:2,000,000円"　同じ位置に同じ変数名はダメ
建物　"譲渡益:1,100,000円"　同じ位置に同じ変数名はダメ
```

 　変数参照で変数パス指定がなければ、まず、見出しで区切られた同じ位置での変数定義がないかを探します。同じ位置になければ、全体から変数定義を探しますが、同じ名前の変数が複数あればエラーになります。

```
# 大分類
"合計:900"
"総合計:1500"
## 中分類
"合計:200"
'合計'　　　　　同じ中分類にある合計200となる
### 小分類
'総合計'       同じ小分類にないので、全体から探し、大分類にある総合計1500になる
'合計'　　　　　同じ小分類にないので、全体から探し、合計が２つあるのでエラーになる
```

　異なる位置にある変数を[変数パス指定](#変数パス指定)を使うことにより、どの変数かを指定することができます。

```
# 大分類
変数定義　"合計:900"
## 中分類
変数定義　"合計:200"
### 小分類１
変数定義　"合計:100"
'#..:合計:200'　　　　　　　「..」は、一つ上の見出し（中分類）を指す
'#細分類:合計:50'　　　　　　自分の下の見出し「細分類」を指す
'#/大分類/中分類:合計:200'　一番上から辿り、「大分類」の下の「中分類」を指す
'#/大分類:合計:900'　　　　　一番上から辿り、「大分類」を指す
'#../小分類２:合計:80'　　　一つ上の見出しの下の「小分類２」を指す
'#.:合計:100'　　　　　　　「.」は自分自身を指す
#### 細分類
変数定義　"合計:50"
### 小分類２
変数定義　"合計:80"
'#//細分類:合計:50'　「//」は辿る道を省略し、どこかにある「細分類」を指します
```

　パスの指定方法は、フォルダの指定方法に似ています。

　パス指定の参考に、変数定義の[変数パス表示](#変数パス表示)や、変数参照の[変数参照パス表示](#変数参照パス表示)を使います。なお、表示される「root」は無視します。

```
# 所得金額
## 利子所得
"国税?/root/所得金額/利子所得:100円"
## 配当所得
"国税?/root/所得金額/配当所得:2,000円"
 　　      /所得金額 が必要なのが分かる
# 税額計算
配当所得の国税を使いたいが、エラーになる
'#/配当所得:国税?:'
   　　　　 　　 /所得金額 が抜けている
配当所得の国税を使いたい、これはOK
'#/所得金額/配当所得:国税?/root/所得金額/配当所得:2,000円'
  /所得金額 を追加
```



　xPath[^3]という技法を使っていますので、他にも色々な指定方法ができます。

[^3]:https://www.techscore.com/tech/XML/XPath/

## エラー処理

　「!」で囲われたエラーメッセージが、変換後テキストに追加されます。

 ==未作成==

## 計算能力

 ==未作成==

累乗　＾

累乗根

精度は、端数処理で指示（|d50|なども可能）



---

計算式　変数参照などを含んだ式

演算式　変数参照などを含まない式（変数に値を代入した後の式）





