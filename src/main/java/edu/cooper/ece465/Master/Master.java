package edu.cooper.ece465.Master;

public class Master {
	
    public static void main(String[] args) {
    	if (args.length != 4){
            System.err.println("Usage: java Master <Client Port Number> <Producer Port Number> <LB Hostname> <LB Port>");
            System.exit(1);
        }

    	int clientPort = Integer.parseInt(args[0]);
    	int producerPort = Integer.parseInt(args[1]);
        String loadBalancerHost = args[2];
        int loadBalancerPort = Integer.parseInt(args[3]);

    	CubbyHole c = new CubbyHole(50);

        new LBChatter(c, loadBalancerHost,loadBalancerPort, clientPort).start();
        new ProducerListener(c, producerPort).start();
    	new ClientListener(c, clientPort).start();
        new ClientProcessorAssigner(c).start();
    }
}
