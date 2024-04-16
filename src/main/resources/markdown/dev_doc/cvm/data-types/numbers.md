Numbers are mostly used for mathematical operations. They can represent quantities, attributes such as age, price and describe many other aspects.
Currently, numbers are represented by 2 data types.

A **long** is a [64-bit signed integer](https://en.wikipedia.org/wiki/Integer_(computer_science)):
- Whole-valued number
- Between -9,223,372,036,854,775,808 and 9,223,372,036,854,775,807 (inclusive).

```clojure
0
1
42

(long? 42)    ;; -> true
(number? 42)  ;; -> true
```

A **double** is a [double precision floating point number](https://en.wikipedia.org/wiki/Double-precision_floating-point_format):
- Has a decimal part
- `##Inf` represents positive infinity
- `##-Inf` represents negative infinity
- `##NaN` represents a failure in some cases (meaning "Not a Number")

```clojure
0.0
1.0
42.0
3.53
##Inf
##-Inf
##NaN

(double? 42.0)  ;; -> true
(number? 42.0)  ;; -> true
```

Doubles are a smaller span than longs data types when it comes to representing whole-valued numbers. Unless a decimal part is needed or a function returns a double, it is advised to use longs.


## Generic functions

Many mathematical functions from the CVM allow mixing longs and doubles:

```clojure
(+ 3 2)      ;; -> 5
(+ 3 2.0)    ;; -> 5.0
(- 3 2.0)    ;; -> 1.0
(* 4 3.5 )   ;; -> 14.0
(/ 3 2)      ;; -> 1.5

(pow 2 3)    ;; -> 8.0, exponentiation, 2 raised to the power of 3
(exp 1)      ;; -> 2.7182818284590455, e raised to the power of 1
(sqrt 4)     ;; -> 2.0, square root of 4.

(signum -42) ;; -> -1, sign is negative
(signum 0)   ;; -> 0
(signum 42)  ;; -> 1, sign is positive
```


## Long functions

Some functions only accept longs:

```clojure
(inc 42)     ;; -> 43, increments given value (adds 1)
(dec 42)     ;; -> 41, decrements given value (subtracts 1)

(rem 10 3)   ;; -> 1, remainder of integer division (10 by 3)
(quot 10 3)  ;; -> 3, quotient, consistent with `rem`
(mod 10 3)   ;; -> 1, integer modulus
```


## Double functions

Some functions accept any number but only really make sense with doubles:

```clojure
(floor 42.49)  ;; -> 42.0
(ceil 42.51)   ;; -> 43.0
```


## Comparisons

These functions serve to compare numbers:

```clojure
(max 1 42.0 -10)  ;; -> 42.0, maximum of all given numbers
(min 1 42.0 -10)  ;; -> -10, minimum of all given numbers

(< 2 3)           ;; -> true, 2 is lesser than 3
(< 2 2)           ;; -> false, 2 is not lesser than 2
(<= 2 2)          ;; -> true, 2 is lesser than OR equal to 2
(> 3 2)           ;; -> true, 3 is greater than 2
(> 3 3)           ;; -> false, 3 is not greater than 3
(>= 3 3)          ;; -> true, 3 is greater than OR equal to 3
```

Checking equality deserves a special note. `=` is the function used to check if 2 arguments are equal but first, they must have the same type. `==` is used for numerical equality, meaning arguments must be numbers
but do not have to have the same type:

```clojure
(= 2 2)     ;; -> true, 2 is perfectly equal to 2
(= 2 2.0)   ;; -> false, 2 is a long while 2.0 is a double
(== 2 2.0)  ;; -> true, mathematically, 2 is equal to 2.0
```


## Predicates

These functions are predicates (returns a [boolean](/cvm/data-types/boolean) related to numbers):

```clojure
(number? 42)      ;; true
(number? "text")  ;; false

(zero? 0)         ;; true
(zero? 42)        ;; false

(long? 42)        ;; true
(long? 42.0)      ;; false, a double

(nan? ##NaN)      ;; true
(nan? 42)         ;; false
```


## Casts

A number type can be cast to another number type if required:

```clojure
(double 45.8)  ;; 45.8
(double 45)    ;; 45.0

(long 42)      ;; 42
(long 42.99)   ;; 42

(long "text")  ;; Error!
```
