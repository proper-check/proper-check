package properchecks;

import java.util.*;

public class CircularBuffer {
  int inp = 0;
  int outp = 0;
  int sz;
  List<Long> list;

  public CircularBuffer(int n) {
    sz = n;
    list = new ArrayList<Long>(n);
    for (int i = 0; i < n; i++) {
      list.add(null);
    }
  }

  public void put(Long n) {
    list.set(inp, n);
    inp = (inp + 1) % sz;
  }

  public Long get() {
    Long res = list.get(outp);
    outp = (outp + 1) % sz;
    return res;
  }

  public int size() {
    return (inp - outp + sz) % sz;
  }
}
