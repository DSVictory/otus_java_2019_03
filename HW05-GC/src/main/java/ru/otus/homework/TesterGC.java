package ru.otus.homework;

import com.sun.management.GarbageCollectionNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import javax.swing.Timer;

/*
������ ���� GC ���������� � ����������� ������
-Xms512m -Xmx512m

������� ����������� �� ����� �������� ��������� ������ � �� ������.

1. -XX:+UseSerialGC
Starting pid: 7340@DESKTOP-LL18RAV
GC name:Copy
GC name:MarkSweepCompact
����� 1 ������... Young ������: 4 (stop �� 934 ms), Old ������: 1 (stop �� 934 ms)
����� 2 ������... Young ������: 2 (stop �� 0 ms), Old ������: 2 (stop �� 2586 ms)
����� 3 ������... Young ������: 0 (stop �� 0 ms), Old ������: 2 (stop �� 3147 ms)
����� 4 ������... Young ������: 0 (stop �� 0 ms), Old ������: 31 (stop �� 44197 ms)
����� 5 ������... Young ������: 0 (stop �� 0 ms), Old ������: 28 (stop �� 39870 ms)
Exception in thread "main"
java.lang.OutOfMemoryError: Java heap space
	at ru.otus.homework.Benchmark.run(Benchmark.java:24)
	at ru.otus.homework.TesterGC.main(TesterGC.java:66)
	
����� ���������� ��������� ��� ��������.
	
2. -XX:+UseParallelGC
Starting pid: 6632@DESKTOP-LL18RAV
GC name:PS MarkSweep
GC name:PS Scavenge
����� 1 ������... Young ������: 4 (stop �� 1079 ms), Old ������: 2 (stop �� 2119 ms)
����� 2 ������... Young ������: 0 (stop �� 0 ms), Old ������: 1 (stop �� 1071 ms)
����� 3 ������... Young ������: 0 (stop �� 0 ms), Old ������: 2 (stop �� 2685 ms)
����� 4 ������... Young ������: 0 (stop �� 0 ms), Old ������: 12 (stop �� 22851 ms)
����� 5 ������... Young ������: 0 (stop �� 0 ms), Old ������: 1 (stop �� 2013 ms)
Exception in thread "main"
java.lang.OutOfMemoryError: GC overhead limit exceeded

�� ��������� � Serial ������������ ���������� ���� � ������ ���������.

3. -XX:+UseG1GC
Starting pid: 14836@DESKTOP-LL18RAV
GC name:G1 Young Generation
GC name:G1 Old Generation
����� 1 ������... Young ������: 14 (stop �� 1170 ms), Old ������: 0 (stop �� 0 ms)
����� 2 ������... Young ������: 6 (stop �� 497 ms), Old ������: 0 (stop �� 0 ms)
����� 3 ������... Young ������: 6 (stop �� 351 ms), Old ������: 0 (stop �� 0 ms)
����� 4 ������... Young ������: 6 (stop �� 362 ms), Old ������: 1 (stop �� 1091 ms)
����� 5 ������... Young ������: 15 (stop �� 492 ms), Old ������: 19 (stop �� 21744 ms)
Exception in thread "main" start:295161 Name:G1 Old Generation, action:end of major GC, gcCause:G1 Evacuation Pause(1200 ms)
java.lang.OutOfMemoryError: Java heap space
	at ru.otus.homework.Benchmark.run(Benchmark.java:24)
	at ru.otus.homework.TesterGC.main(TesterGC.java:85)


�� ���� �������� ��������� �������� � ������� ���������� ���� � ������ ���������,
��� ����, ����������� ����� ��������� ����, ��������� � �����, ��� �� � ������� �� ������ ���������
������ �������� ���� ���������, �� ��� ���� ������ ��������. ���������� �������.

4. -XX:+UnlockExperimentalVMOptions -XX:+UseZGC 
Error occurred during initialization of VM
Option -XX:+UseZGC not supported

����� 11 java, ���� � ����� Oracle, 

 */

public class TesterGC {

	public static int youngCountInMinute;
	public static long youngDuration;
	
	public static int oldCountInMinute;
	public static long oldDuration;
	
	public static int currentMinute = 1;
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting pid: " + ManagementFactory.getRuntimeMXBean().getName());
        switchOnMonitoring();

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("ru.otus.homework:type=Benchmark");
        Benchmark mbean = new Benchmark(0, 16, 10);
        mbs.registerMBean(mbean, name);
        
        Timer timer = new Timer(60000, x -> {
        	System.out.printf("����� %s ������... Young ������: %d (stop �� %d ms), Old ������: %d (stop �� %d ms)\n",
        			currentMinute,
	    			youngCountInMinute,
	    			youngDuration,
	    			oldCountInMinute,
	    			oldDuration);
        	currentMinute++;
        	youngCountInMinute = 0;
        	youngDuration = 0;
        	oldCountInMinute = 0;
        	oldDuration = 0;
        });
        timer.start();
        mbean.run();
	}
	
	private static void switchOnMonitoring() {
        List<GarbageCollectorMXBean> gcbeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcbean : gcbeans) {
            System.out.println("GC name:" + gcbean.getName());
            NotificationEmitter emitter = (NotificationEmitter) gcbean;
            NotificationListener listener = (notification, handback) -> {
                if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                    GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                    
                    //String gcName = info.getGcName();
                    long duration = info.getGcInfo().getDuration();
                    
                    String gcAction = info.getGcAction();
     

                    if (gcAction.equals("end of minor GC")) {
                    	youngCountInMinute++;
                    	youngDuration += duration;
                    }
                    if (gcAction.equals("end of major GC")) {
                    	oldCountInMinute++;
                    	oldDuration += duration;
                    }
                    
                    String gcName = info.getGcName();
                    String gcCause  = info.getGcCause();

                    long start = info.getGcInfo().getStartTime();

                    System.out.println("start:" + start + " Name:" + gcName + ", action:" + gcAction + ", gcCause:" + gcCause + "(" + duration + " ms)");
                    
                }
            };
            emitter.addNotificationListener(listener, null, null);
        }
    }

}
