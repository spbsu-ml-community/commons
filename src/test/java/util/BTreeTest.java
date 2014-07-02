//package util;
//
//import com.spbsu.commons.io.StreamTools;
//import com.spbsu.commons.func.converters.Integer2ByteBufferConverter;
//import gnu.trove.TIntLongHashMap;
//import junit.framework.TestCase;
//
//import java.io.File;
//import java.nio.ByteBuffer;
//import java.util.*;
//
///**
// * User: igorkuralenok
// * Date: 08.04.2008
// */
//@SuppressWarnings({"unchecked"})
//public class BTreeTest extends TestCase {
//  private File testRoot;
//  private static final int[] SAMPLE_32K_ARRAY = new int[32768 / 4];
//
//  static {
//    for(int i = 0; i < SAMPLE_32K_ARRAY.length; i++){
//      SAMPLE_32K_ARRAY[i] = (int)(Math.random() * 1000000.0);
//    }
//  }
//
//  protected void setUp() throws Exception {
//    testRoot = File.createTempFile("test", "");
//    testRoot.delete();
//    testRoot.mkdirs();
//    testRoot.mkdir();
//  }
//
//  protected void tearDown() throws Exception {
//    if(testRoot != null) StreamTools.deleteDirectoryWithContents(testRoot);
//  }
//
////  public void testPageCreation() throws Exception{
////    final File storage = new File(testRoot, "storage");
////    final BTreeMap<Integer, Integer> treeMap = new BTreeMap<Integer, Integer>(
////            storage,
////            new Integer2ByteBufferConverter(),
////            new Integer2ByteBufferConverter(),
////            100);
////    assertNull(treeMap.getPage(1));
////    assertTrue(treeMap.createPage(EmptyPage.class) != null);
////    assertEquals(2 * BTreeMap.SLOT_CAPACITY, storage.length());
////
////    treeMap.createPage(EmptyPage.class);
////    assertEquals(3 * BTreeMap.SLOT_CAPACITY, storage.length());
////
////    treeMap.flush();
////
////    treeMap.getPage(1);
////    treeMap.getPage(2);
////  }
//
//  public void testCreate() throws Exception{
//    final File storage = new File(testRoot, "storage");
//    BTreeMap treeMap = new BTreeMap<Integer, Integer>(storage, new Integer2ByteBufferConverter(), new Integer2ByteBufferConverter(), 100);
//
//    assertTrue(storage.exists());
//    assertEquals(storage.length(), BTreeMap.SLOT_CAPACITY);
//
//    treeMap.close();
//
//    assertTrue(storage.exists());
//    assertEquals(storage.length(), BTreeMap.SLOT_CAPACITY);
//
//    assertTrue(treeMap.isEmpty());
//    assertEquals(treeMap.size(), 0);
//
//    treeMap = new BTreeMap(storage, new Integer2ByteBufferConverter(), new Integer2ByteBufferConverter(), 100);
//
//    assertTrue(storage.exists());
//    assertEquals(storage.length(), BTreeMap.SLOT_CAPACITY);
//
//    treeMap.close();
//  }
//
//  public void testPut8BData() throws Exception{
//    final File storage = new File(testRoot, "storage");
//    BTreeMap<Integer, Integer> treeMap = new BTreeMap<Integer, Integer>(storage, new Integer2ByteBufferConverter(), new Integer2ByteBufferConverter(), 100);
//    treeMap.put(1, 0);
//    assertEquals(1, treeMap.size());
//    assertTrue(treeMap.containsKey(1));
//    assertEquals(0, (int)treeMap.at(1));
//  }
//
//  public void testPut2KbData() throws Exception{
//    final File storage = new File(testRoot, "storage");
//    BTreeMap<Integer, int[]> treeMap = new BTreeMap<Integer, int[]>(storage, new Integer2ByteBufferConverter(), new IntegerArray2ByteBufferConverter(), 100);
//    treeMap.put(1, SAMPLE_32K_ARRAY);
//    assertEquals(1, treeMap.size());
//    assertTrue(treeMap.containsKey(1));
//    assertTrue(Arrays.equals(SAMPLE_32K_ARRAY, treeMap.at(1)));
//    treeMap.close();
//    treeMap = new BTreeMap<Integer, int[]>(storage, new Integer2ByteBufferConverter(), new IntegerArray2ByteBufferConverter(), 100);
//    assertTrue(Arrays.equals(SAMPLE_32K_ARRAY, treeMap.at(1)));
//  }
//
//  public void testPut8PagesOfData() throws Exception{
//    final File storage = new File(testRoot, "storage");
//    BTreeMap<Integer, Integer> treeMap = new BTreeMap<Integer, Integer>(storage, new Integer2ByteBufferConverter(), new Integer2ByteBufferConverter(), 100);
//    for(int i = 0; i < 16384; i++){
//      treeMap.put(i, i);
//    }
//    assertEquals(16384, treeMap.size());
//    assertTrue(treeMap.containsKey(1));
//    for(int i = 0; i < 16384; i++){
//      assertEquals(i, (int)treeMap.at(i));
//    }
//    treeMap.close();
//    treeMap = new BTreeMap<Integer, Integer>(storage, new Integer2ByteBufferConverter(), new Integer2ByteBufferConverter(), 100);
//    for(int i = 0; i < 16384; i++){
//      assertEquals(i, (int)treeMap.at(i));
//    }
//  }
//
//  public void testPut800PagesOfData() throws Exception{
//    Interval.start();
//    final File storage = new File(testRoot, "storage");
//    BTreeMap<Integer, Integer> treeMap = new BTreeMap<Integer, Integer>(storage, new Integer2ByteBufferConverter(), new Integer2ByteBufferConverter(), 100);
//    for(int i = 0; i < 51200; i++){
//      treeMap.put(i, i);
//    }
//    assertEquals(51200, treeMap.size());
//    assertTrue(treeMap.containsKey(1));
//    System.out.println(Interval.time());
//    for(int i = 0; i < 51200; i++){
//      assertEquals(i, (int)treeMap.at(i));
//    }
//    treeMap.close();
//    Interval.stopAndPrint();
//    Interval.start();
//    treeMap = new BTreeMap<Integer, Integer>(storage, new Integer2ByteBufferConverter(), new Integer2ByteBufferConverter(), 100);
//    for(int i = 0; i < 51200; i++){
//      assertEquals(i, (int)treeMap.at(i));
//    }
//    Interval.stopAndPrint();
//  }
//
//  public void testPut80LongPagesOfData() throws Exception{
//    final File storage = new File(testRoot, "storage");
//    Interval.start();
//
//    BTreeMap<Integer, int[]> treeMap = new BTreeMap<Integer, int[]>(storage, new Integer2ByteBufferConverter(), new IntegerArray2ByteBufferConverter(), 100);
//    for(int i = 0; i < 5120; i++){
//      treeMap.put(i, SAMPLE_32K_ARRAY);
//    }
//    assertEquals(5120, treeMap.size());
//    System.out.println(Interval.time());
//    assertTrue(treeMap.containsKey(1));
//    for(int i = 0; i < 5120; i++){
//      assertTrue(Arrays.equals(SAMPLE_32K_ARRAY, treeMap.at(i)));
//    }
//    treeMap.close();
//    Interval.stopAndPrint();
//    Interval.start();
//    treeMap = new BTreeMap<Integer, int[]>(storage, new Integer2ByteBufferConverter(), new IntegerArray2ByteBufferConverter(), 100);
//    for(int i = 0; i < 5120; i++){
//      assertTrue(Arrays.equals(SAMPLE_32K_ARRAY, treeMap.at(i)));
//    }
//    Interval.stopAndPrint();
//  }
//
//  public void testFirstPage() throws Exception {
//    final File storage = new File(testRoot, "storage");
//    final Random random = new Random();
//    Interval.start();
//    final IntegerArray2ByteBufferConverter converter = new IntegerArray2ByteBufferConverter();
//
//    BTreeMap<Integer, int[]> treeMap = new BTreeMap<Integer, int[]>(storage, new Integer2ByteBufferConverter(), new IntegerArray2ByteBufferConverter(), 100);
//    Map<Long, Integer> length = new BTreeMap<Long, Integer>(new File(testRoot, "length"), new Long2ByteBufferConverter(), new Integer2ByteBufferConverter(), 10000);
//    long[] index = new long[PageManager.degree(BTreeMap.SLOT_CAPACITY - 1) + 1];
//    long maxAddress = 0;
//
//    for(int i = 0; i < 100000; i++){
//      double rand = random.nextGaussian();
//      int size = (int)(rand * rand * 100) + 1;
//      if(size > 100000) size = 100000;
//
//      final int[] array = new int[size];
//      for (int j = 0; j < array.length; j++) array[j] = j;
//
//      ByteBuffer buffer = converter.convertFrom(array);
//      final int bufferLength = buffer.remaining();
//      final DataPage page = treeMap.pageManager.nextDataPage(bufferLength);
//      final long address = page.writeData(buffer);
//      final int degree = Math.min(PageManager.degree(bufferLength), index.length - 1);
//      if((address & 0xFFFFFF) != (index[degree] & 0xFFFFFF)){
//        if((address & 0xFFFFFF) < maxAddress || (0xFFFF & address >> 24) > 0)
//          assertTrue(false); // addresses must be consequent
//      }
//      else if(((address >> 24) & 0xFFFF) != ((index[degree]>> 24) & 0xFFFF) + 1)
//        assertTrue(false); // addresses must be consequent
//      index[degree] = address;
//      maxAddress = Math.max(maxAddress, address & 0xffffff);
//      if(length.containsKey(address))
//        assertTrue(false);
//      length.put(address, size);
//
//      buffer = ((DataPage)treeMap.getPage(address)).readData(address);
//      int[] read = converter.convertTo(buffer);
//      if(!Arrays.equals(array, read))
//        assertTrue(Arrays.equals(array, read));
//    }
//    for (Map.Entry<Long, Integer> entry : length.entrySet()) {
//      final int[] array = new int[entry.getValue()];
//      for (int j = 0; j < array.length; j++) array[j] = j;
//      long address = entry.getKey();
//      final ByteBuffer buffer = ((DataPage)treeMap.getPage(address)).readData(address);
//      int[] read = converter.convertTo(buffer);
//      if(!Arrays.equals(array, read))
//        assertTrue(Arrays.equals(array, read));
//    }
//  }
//
//  public void testPerformance() throws Exception{
//    final File storage = new File(testRoot, "storage");
//    final Random random = new Random();
//    Interval.start();
//
//    BTreeMap<Integer, int[]> treeMap = new BTreeMap<Integer, int[]>(storage, new Integer2ByteBufferConverter(), new IntegerArray2ByteBufferConverter(), 100);
//    int[] length = new int[1000000];
//    double originalMean = 0;
//    for(int i = 0; i < 1000000; i++){
//      double rand = random.nextGaussian();
//
//      int size = (int)(rand * rand * 100);
//      if(size > 100000) size = 100000;
//      length[i] = size;
//      originalMean += size;
//      final int[] array = new int[size];
//      for (int j = 0; j < array.length; j++) {
//        array[j] = j;
//      }
//      treeMap.put(i, array);
//      if(!Arrays.equals(treeMap.at(i), array))
//        assertTrue(Arrays.equals(treeMap.at(i), array));
//    }
//    Interval.stopAndPrint();
//    Interval.start();
//    double mean = 0;
//    for(int i = 0; i < 1000000; i++){
//      int[] array = treeMap.at(i);
//      if(array == null){
//        treeMap.at(i);
//        assertTrue("NULL array!", false);
//      }
//      for (int j = 0; j < array.length; j++) {
//        assertEquals(j, array[j]);
//      }
//      mean += array.length;
//    }
//    assertEquals(mean, originalMean);
//    Interval.stopAndPrint();
//    System.out.println(mean / 1000000);
//  }
//
//  private static char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ�������������������������������߀������������������������������� -;.\"',!".toCharArray();
//
//  public void testStrings() throws Exception {
//    final File storage = new File(testRoot, "storage");
//    final Random random = new Random();
//    Interval.start();
//
//    BTreeMap<Integer, String> invertedMap = new BTreeMap<Integer, String>(storage, new Integer2ByteBufferConverter(), new String2ByteBufferConverter(), 100);
//    int[] crc = new int[100000];
//
//    for(int i = 0; i < 100000; i++){
//      double rand = random.nextGaussian();
//
//      int size = (int)(rand * rand * 20);
//      if(size > 1000) size = 1000;
//      final char[] array = new char[size];
//      for (int j = 0; j < array.length; j++) {
//        array[j] = chars[((int) (Math.random() * (chars.length - 1)))];
//        crc[i] += array[j];
//      }
//      final String str = new String(array);
//      invertedMap.put(i, str);
//    }
//
//    for(int i = 0; i < 100000; i++){
//      final String str = invertedMap.at(i);
//      int index = 0;
//      int summ = 0;
//      while(index < str.length()) summ += str.charAt(index++);
//      assertEquals(crc[i], summ);
//    }
//  }
//
//  public void testStringsKey() throws Exception {
//    final File storage = new File(testRoot, "storage");
//    final Random random = new Random();
//    Interval.start();
//
//    BTreeMap<String, Integer> treeMap = new BTreeMap<String, Integer>(storage, new String2ByteBufferConverter(), new Integer2ByteBufferConverter(), 100);
//
//    for(int i = 0; i < 100000; i++){
//      int crc = 0;
//      double rand = random.nextGaussian();
//
//      int size = (int)(rand * rand * 20);
//      if(size > 1000) size = 1000;
//      final char[] array = new char[size];
//      for (int j = 0; j < array.length; j++) {
//        array[j] = chars[((int) (Math.random() * (chars.length - 1)))];
//        crc += array[j];
//      }
//      final String str = new String(array);
//      treeMap.put(str, crc);
//    }
//
//    for (Map.Entry<String, Integer> entry : treeMap.entrySet()) {
//      final String str = entry.getKey();
//      int index = 0;
//      int summ = 0;
//      while(index < str.length()) summ += str.charAt(index++);
//      assertEquals(summ, treeMap.at(str).intValue());
//    }
//  }
//
//  public void testMergeEmpty(){
//    TIntLongHashMap map = new TIntLongHashMap();
//    int[] keys = new int[]{};
//    long[] values = new long[]{};
//    int[] resultKeys = new int[0];
//    long[] resultValues = new long[0];
//    LeafPage.mergeArrayWithHash(resultKeys, resultValues, keys, values, map);
//  }
//
//  public void testMergeNewValuesOnly(){
//    TIntLongHashMap map = new TIntLongHashMap();
//    int[] keys = new int[]{};
//    long[] values = new long[]{};
//    int[] resultKeys = new int[1];
//    long[] resultValues = new long[1];
//    map.put(1, 2);
//    LeafPage.mergeArrayWithHash(resultKeys, resultValues, keys, values, map);
//    assertEquals(1, resultKeys.length);
//    assertEquals(1, resultKeys[0]);
//    assertEquals(2, resultValues[0]);
//  }
//
//  public void testMerge1(){
//    TIntLongHashMap map = new TIntLongHashMap();
//    int[] keys = new int[]{1};
//    long[] values = new long[]{2};
//    int[] resultKeys = new int[2];
//    long[] resultValues = new long[2];
//    map.put(2, 1);
//    LeafPage.mergeArrayWithHash(resultKeys, resultValues, keys, values, map);
//    assertEquals(1, resultKeys[0]);
//    assertEquals(2, resultKeys[1]);
//    assertEquals(2, resultValues[0]);
//    assertEquals(1, resultValues[1]);
//  }
//
//  public void testMerge2(){
//    TIntLongHashMap map = new TIntLongHashMap();
//    int[] keys = new int[]{2};
//    long[] values = new long[]{1};
//    int[] resultKeys = new int[2];
//    long[] resultValues = new long[2];
//    map.put(1, 2);
//    LeafPage.mergeArrayWithHash(resultKeys, resultValues, keys, values, map);
//    assertEquals(1, resultKeys[0]);
//    assertEquals(2, resultKeys[1]);
//    assertEquals(2, resultValues[0]);
//    assertEquals(1, resultValues[1]);
//  }
//
//  public void testMerge3(){
//    TIntLongHashMap map = new TIntLongHashMap();
//    int[] keys = new int[]{2, 5, 8};
//    long[] values = new long[]{1, 1, 1};
//    int[] resultKeys = new int[6];
//    long[] resultValues = new long[6];
//    map.put(1, 2);
//    map.put(3, 2);
//    map.put(7, 2);
//    LeafPage.mergeArrayWithHash(resultKeys, resultValues, keys, values, map);
//    assertEquals(1, resultKeys[0]);
//    assertEquals(2, resultKeys[1]);
//    assertEquals(3, resultKeys[2]);
//    assertEquals(5, resultKeys[3]);
//    assertEquals(7, resultKeys[4]);
//    assertEquals(8, resultKeys[5]);
//  }
//
//  public void testMergeReplace(){
//    TIntLongHashMap map = new TIntLongHashMap();
//    int[] keys = new int[]{2, 5, 8};
//    long[] values = new long[]{1, 1, 1};
//    int[] resultKeys = new int[5];
//    long[] resultValues = new long[5];
//    map.put(1, 2);
//    map.put(5, 2);
//    map.put(7, 2);
//    LeafPage.mergeArrayWithHash(resultKeys, resultValues, keys, values, map);
//    assertEquals(1, resultKeys[0]);
//    assertEquals(2, resultKeys[1]);
//    assertEquals(5, resultKeys[2]);
//    assertEquals(7, resultKeys[3]);
//    assertEquals(8, resultKeys[4]);
//  }
//
//  public void testMergeStress(){
//    for(int i = 0; i < 10000; i++){
//      int sizeNew = (int) (Math.random() * 1000);
//      int sizeOld = (int) (Math.random() * 10000);
//      int[] old = new int[sizeOld];
//      long[] oldValues = new long[sizeOld];
//      Arrays.fill(oldValues, 1);
//      Set<Integer> inIndex = new HashSet<Integer>();
//      for(int j = 0; j < sizeOld; j++){
//        int next = 0;
//        while (inIndex.contains(next))
//          next = (int) (Math.random() * 1000000);
//        inIndex.add(next);
//        old[j] = next;
//      }
//      Arrays.sort(old);
//
//      TIntLongHashMap map = new TIntLongHashMap();
//      Arrays.fill(oldValues, 1);
//      for(int j = 0; j < sizeNew; j++){
//        final int next = (int) (Math.random() * 1000000);
//        if(inIndex.contains(next) && !map.contains(next)) sizeOld --;
//        map.put(next, 2l);
//      }
//      int[] result = new int[sizeOld + map.size()];
//      long[] resultValues = new long[sizeOld + map.size()];
//      LeafPage.mergeArrayWithHash(result, resultValues, old, oldValues, map);
//      final int[] clone = result.clone();
//      Arrays.sort(clone);
//      if(!Arrays.equals(clone, result))
//        assertTrue(Arrays.equals(clone, result));
//    }
//  }
//}
