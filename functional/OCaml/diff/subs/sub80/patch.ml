type aexp =
  | Const of int
  | Var of string
  | Power of (string * int)
  | Times of aexp list
  | Sum of aexp list

let rec diff ((exp : aexp), (var : string)) : aexp =
  match exp with
  | Const c -> Const 0
  | Var v -> if v = var then Const 1 else Const 0
  | Power (st, i) ->
      if st = var then Times [ Const i; Power (st, i - 1) ] else Const 0
  | Sum h :: t ->
      let diffHelp (alexp : aexp) : aexp = diff (alexp, var) in
      Sum (diff (h, var) :: List.map diffHelp t)
  | Times __s62 -> (
      match __s62 with
      | [] -> Times []
      | [ __s63 ] -> diff (__s63, var)
      | __s64 :: __s65 ->
          Sum
            [
              Times (diff (__s64, var) :: __s65);
              Times [ __s64; diff (Times __s65, var) ];
            ] )
  | _ -> exp
