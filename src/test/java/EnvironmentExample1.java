import static java.lang.System.*;

import java.util.*;

public class EnvironmentExample1 {
  void EX1() {
    @NestedEnvironment({}) @FlatEnvironment({}) final String s = "a";
    s.equals("a");
    "a".equals(s);
    @NestedEnvironment({ "EX1.s#String" }) @FlatEnvironment({ "s" }) int a = 0;
    out.print("a");
    @NestedEnvironment({ "EX1.a#int", "EX1.s#String" }) @FlatEnvironment({ "a", "s" }) final int b = 0;
    @Begin class A {
    }
    ++a;
    @End("a") class B {
    }
    @NestedEnvironment({ "EX1.a#int", "EX1.s#String", "EX1.b#int" }) @FlatEnvironment({ "a", "s", "b" }) final int c = 0;
  }

  {
    @Begin class A {
    }
    EX2.x = 0;
    @End("x") class B {
    }
  }

  public static class EX2 { // initializator
    @NestedEnvironment({}) @FlatEnvironment({}) static int x;
    @NestedEnvironment({ "EX2.x#int" }) @FlatEnvironment({ "x" }) int y;

    EX2() {
      @Begin class A {
      }
      x = 1;
      @End("x") class B {
      }
    }

    {
      @Begin class A {
      }
      C1.x = 2;
      @End("x") class B {
      }
    }

    @FlatEnvironment({ "x", "y" }) static class C1 {
      @NestedEnvironment({ "EX2.C1.x#int" }) @FlatEnvironment({ "x" }) public static int y; // no
                                                                                            // 'y'
                                                                                            // cause
                                                                                            // it
                                                                                            // is
                                                                                            // a
                                                                                            // static
                                                                                            // class
                                                                                            // (x
                                                                                            // is
                                                                                            // static
                                                                                            // also)
      C1 c1;
      @NestedEnvironment({ "EX2.C1.x#int", "EX2.C1.y#int", "EX2.C1.c1#C1" }) @FlatEnvironment({ "x", "y", "c1" }) public static int x;

      public static void change_x() {
        @Begin class A {
        }
        x = 3; // interesting... what does it do? lol
        @End("x") class B {
        }
      }

      public static void change_y() {
        @Begin class A {
        }
        y = 3;
        @End("x") class B {
        }
      }
    }
  }

  public static class EX3 { // hiding
    @NestedEnvironment({}) @FlatEnvironment({}) int x, y;

    EX3() {
      @Begin class A {
      }
      x = y = 0;
      @End({ "x", "y" }) class B {
      }
      @Begin class C {
      }
      y = 1;
      x = 2;
      @End({ "x", "y" }) class D {
      }
    }

    @NestedEnvironment({ "EX3.x", "EX3.y" }) @FlatEnvironment({ "x", "y" }) static class x_hiding {
      @FlatEnvironment({}) public static int x;
      @NestedEnvironment({ "EX3.x_hiding.x#int" }) @FlatEnvironment({ "x" }) y_hiding xsy;

      x_hiding() {
        x = 2;
        xsy = new y_hiding();
      }

      @NestedEnvironment({ "EX3.x_hiding.x#int", "EX3.x_hiding.xsy#y_hiding" }) @FlatEnvironment({ "x", "xsy" }) public class y_hiding { // not
                                                                                                                                         // static
                                                                                                                                         // in
                                                                                                                                         // purpose!
        @FlatEnvironment({ "x", "xsy" }) public int y;

        @Begin class C {
        }

        y_hiding() {
          @Begin class E {
          }
          y = 2;
          @End({ "y" }) class F {
          }
        }

        @End({ "y" }) class D {
        }
      }
    }

    @NestedEnvironment({ "EX3.x", "EX3.y" }) @FlatEnvironment({ "x", "y" }) int q; // should
                                                                                   // not
                                                                                   // recognize
                                                                                   // xsy

    static void func() {
      @Begin class Q {
      }
      final EX3 top = new EX3();
      final x_hiding X = new x_hiding();
      final x_hiding.y_hiding Y = X.new y_hiding();
      top.x = 3;
      x_hiding.x = 4;
      X.xsy.y = 5;
      Y.y = 6;
      @End({ "top", "X", "x", "xsy", "Y", "y" }) class QQ {
      }
    }
  }

  public static class EX4 { // Inheritance
    @FlatEnvironment({}) int x;

    class Parent {
      @Begin class Q {
      }

      Parent() {
        x = 0;
      }

      @End({ "x" }) class QQ {
      }

      void set_x() {
        @Begin class Q {
        }
        x = 1;
        @End({ "x" }) class QQ {
        }
      }
    }

    class Child1 extends Parent {
      Child1() {
        @Begin class Q {
        }
        x = 2;
        @End({ "x" }) class QQ {
        }
      }

      @Override void set_x() {
        @Begin class Q {
        }
        x = 3;
        @End({ "x" }) class QQ {
        }
      }
    }

    class Child2 extends Parent {
      int x;

      Child2() {
        x = 4;
      }

      @Override void set_x() {
        @Begin class Q {
        }
        x = 5;
        @End({ "x" }) class QQ {
        }
      }
    }

    void func() {
      @Begin class Q {
      }
      @FlatEnvironment({ "x" }) final Parent p = new Parent();
      @FlatEnvironment({ "x", "p" }) final Child1 c1 = new Child1();
      @NestedEnvironment({ "EX4.x#int", "EX4.p#Parent", "EX4.c1#C1" }) @FlatEnvironment({ "x", "p", "c1" }) final Child2 c2 = new Child2();
      p.set_x();
      c1.set_x();
      c2.set_x();
      @End({ "x" }) class QQ {
      }
    }
  }

  {
    EX5.x = 0;
  }

  @FlatEnvironment({ "x" }) public static class EX5 {
    static int x;

    @FlatEnvironment({ "x" }) class a {
      int a_x;

      @FlatEnvironment({ "x", "a_x" }) class b {
        int b_x;

        @FlatEnvironment({ "x", "a_x", "b_x" }) class c {
          int c_x;

          @FlatEnvironment({ "x", "a_x", "b_x", "c_x" }) class d {
            int d_x;

            @FlatEnvironment({ "x", "a_x", "b_x", "c_x", "d_x" }) void d_func() {
              @Begin class opening {
                /**/}
              ++a_x;
              ++b_x;
              ++c_x;
              ++d_x;
              @End({ "a_x", "b_x", "c_x", "d_x" }) class closing {
                /**/}
            }
          }

          @FlatEnvironment({ "x", "a_x", "b_x", "c_x" }) void c_func() {
            @Begin class opening {
              /**/}
            ++a_x;
            ++b_x;
            ++c_x;
            @End({ "a_x", "b_x", "c_x" }) class closing {
              /**/}
          }
        }

        @FlatEnvironment({ "x", "a_x", "b_x" }) void b_func() {
          @Begin class opening {
            /**/}
          ++a_x;
          ++b_x;
          @End({ "a_x", "b_x" }) class closing {
            /**/}
        }
      }

      @FlatEnvironment({ "x", "a_x", "b_x" }) void a_func() {
        @Begin class opening {
          /**/}
        ++a_x;
        @End({ "a_x" }) class closing {
          /**/}
      }
    }
  }

  public static class EX6 {
    @NestedEnvironment({}) @FlatEnvironment({}) class Outer {
      int x;

      @NestedEnvironment({ "EX6.Outer.x#int" }) @FlatEnvironment({ "x" }) class Inner {
        final Outer outer = Outer.this; // Supposedly, this should allow us to
                                        // access the outer x.

        @NestedEnvironment({ "EX6.Outer.x#int", "EX6.Outer.Inner.outer#Outer" }) @FlatEnvironment({ "x", "outer" }) void func(final Inner p) {
          @Begin class m {
            /**/}
          // working on the current instance
          x = 0;
          x = 1;
          // working on another instance
          p.outer.x = 2;
          @End({ "x" }) class n {
            /**/}
        }
      }
    }

    class Outer2 {
      int x;

      @NestedEnvironment({ "EX6.Outer2.x#int" }) @FlatEnvironment({ "x" }) class Inner2 {
        int x;
        final Outer2 outer2 = Outer2.this;

        @NestedEnvironment({ "EX6.Outer2.x#int", "EX6.Outer2.Inner2.x#int", "EX6.Outer2.Inner2.outer2#Outer2" }) @FlatEnvironment({ "x",
            "outer2" }) void func(final Inner2 p) {
          @Begin class A {
            /**/}
          x = 0;
          Outer2.this.x = 1;
          p.outer2.x = 2;
          @End({ "x" }) class B {
            /**/}
        }
      }
    }
  }

  public static class EX7 { // func_param_name_to_ENV
    Integer x = 1;

    class Complex {
      int r;
      int i;
    }

    static Integer func(final Integer n1, final String n2, final Complex n3) {
      @FlatEnvironment({ "n1", "n2", "n3" }) final int q;
      return n1;
    }

    Integer o = func(x, "Alex&Dan", new Complex());
  }

  public static class EX8 {
    class Arr {
      String[] arr;

      @NestedEnvironment({ "EX8.Arr.arr#String[]" }) @FlatEnvironment({ "arr" }) void foo() {
        @Begin class m {
          /**/}
        arr[2] = "$$$";
        @End({ "arr" }) class n {
          /**/}
      }
    }
  }

  public static class EX9 { // template
    public class SOList<Type> implements Iterable<Type> {
      private final Type[] arrayList;
      @FlatEnvironment({ "arrayList" }) int currentSize;

      @FlatEnvironment({ "arrayList", "currentSize" }) public SOList(final Type[] newArray) {
        @Begin class opening {
          /**/}
        this.arrayList = newArray;
        this.currentSize = arrayList.length;
        @End({ "arrayList", "currentSize" }) class closing {
          /**/}
      }

      @Override public Iterator<Type> iterator() {
        final Iterator<Type> $ = new Iterator<Type>() {
          @FlatEnvironment({ "arrayList", "currentSize", "it" }) private int currentIndex = 0;

          @Override public boolean hasNext() {
            return currentIndex < currentSize && arrayList[currentIndex] != null;
          }

          @Override public Type next() {
            return arrayList[currentIndex++];
          }

          @Override public void remove() {
            throw new UnsupportedOperationException();
          }
        };
        @FlatEnvironment({ "arrayList", "currentSize" }) final int q; // currentIndex
        // shouldn't be
        // recognized
        return $;
      }
    }
  }

  // for the end
  public static class EX99 { // for_testing_the_use_of_names
    class Oompa_Loompa {
      Oompa_Loompa Oompa_Loompa; /* A */

      <Oompa_Loompa> Oompa_Loompa() {
      }

      Oompa_Loompa(final Oompa_Loompa... Oompa_Loompa) {
        this(Oompa_Loompa, Oompa_Loompa);
      }

      Oompa_Loompa(final Oompa_Loompa[]... Oompa_Loompa) {
        this();
      }

      Oompa_Loompa Oompa_Loompa(final Oompa_Loompa Oompa_Loompa) {
        Oompa_Loompa: for (;;)
          for (;;)
            if (new Oompa_Loompa(Oompa_Loompa) { /* D */
              @Override Oompa_Loompa Oompa_Loompa(final Oompa_Loompa Oompa_Loompa) {
                return Oompa_Loompa != null ? /* C */
                super.Oompa_Loompa(Oompa_Loompa) /* B */
                    : Oompa_Loompa.this.Oompa_Loompa(Oompa_Loompa);
              }
            }.Oompa_Loompa(Oompa_Loompa) != null)
              break Oompa_Loompa;
            else
              continue Oompa_Loompa;
        return Oompa_Loompa;
      }
    }
  }
}