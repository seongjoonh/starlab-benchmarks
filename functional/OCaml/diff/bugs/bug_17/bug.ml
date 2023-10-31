exception InvalidArgument

exception L2AL_null

type aexp =
  | Const of int
  | Var of string
  | Power of (string * int)
  | Times of aexp list
  | Sum of aexp list

let rec diff ((aexp : aexp), (str : string)) : aexp =
  let rec list2aexplist ((aexplist : aexp list), (st : string)) : aexp list =
    match aexplist with
    | [ h ] -> [ diff (h, st) ]
    | h :: t -> [ diff (h, st) ] @ list2aexplist (t, st)
    | [] -> raise L2AL_null
  in

  match aexp with
  | Const c -> Const 0
  | Var s -> if s = str then Const 1 else Const 0
  | Power (s, n) ->
      if n = 0 then Const 1
      else if n = 1 && s = str then Const 1
      else if n = 1 && s != str then Const 0
      else if s = str then Times [ Const n; Power (s, n - 1) ]
      else Const 0
  | Sum aexplist -> (
      match aexplist with
      | [] -> raise InvalidArgument
      | [ h ] -> diff (h, str)
      | h :: t -> Sum ([ diff (h, str) ] @ list2aexplist (t, str)) )
  | Times aexplist -> (
      match aexplist with
      | [] -> raise InvalidArgument
      | [ h ] -> diff (h, str)
      | h :: t ->
          Sum
            [
              Times ([ diff (h, str) ] @ t);
              Times ([ h ] @ [ diff (Times t, str) ]);
            ] )
