type aexp =
  | Const of int
  | Var of string
  | Power of (string * int)
  | Times of aexp list
  | Sum of aexp list

let rec diff ((exp : aexp), (x : string)) : aexp =
  match exp with
  | Const p -> Const 0
  | Var p -> if p = x then Const 1 else Const 0
  | Power (p, n) ->
      if p = x then Times [ Const n; Power (p, n - 1) ] else Const 0
  | Times p -> jun1 p x
  | Sum p -> Sum (jun p x)


and jun1 (p : aexp list) (x : string) : aexp =
  match p with
  | [] -> Const 0
  | hd :: tl ->
      Sum
        [ Times (diff (hd, x) :: tl); Times ([ hd ] @ [ diff (Times tl, x) ]) ]


and jun (q : aexp list) (x : string) : aexp list =
  match q with
  | [] -> []
  | hd :: tl -> (
      let a : aexp = hd and b : aexp list = tl in

      match b with
      | [] -> [ diff (a, x) ]
      | hd :: tl -> [ diff (a, x) ] @ [ diff (hd, x) ] @ jun tl x
      | [] -> [] )


let (_ : aexp) =
  diff (Sum [ Power ("x", 2); Times [ Const 2; Var "x" ]; Const 1 ], "x")
