import java.util.*;
import java.io.*;
public class Main
{
  public void levelizeCircuit(ArrayList<Node> nodes)
  {
    for(Node node : nodes)
    {
      if(node.isGate())
      {
        if(node.gate_type.equalsIgnoreCase("NOT"))
        {
          Node prevNode = node.prevNodes.get(0);
          node.level = prevNode.level+1;
        }
        else
        {
          Node prevNode1 = node.prevNodes.get(0);
          Node prevNode2 = node.prevNodes.get(1);
          node.level = Math.max(prevNode1.level,prevNode2.level)+1;
        }
      }
    }
  }

  public void setFaultList(Node node)
  {
    Set<String> common = new HashSet<String>();
    node.L.clear();
    
    if(node.gate_type.equalsIgnoreCase("NOT"))
    {
      Node prevNode = node.prevNodes.get(0);
      node.L.addAll(prevNode.L);
    }
    else
    {
      Node prevNode1 = node.prevNodes.get(0);
      Node prevNode2 = node.prevNodes.get(1);
      for(int i=0; i<4; i++)
      {
        int in1 = (i&(1<<1))==0?0:1;
        int in2 = (i&(1<<0))==0?0:1;
        if(node.out != node.output(in1, in2))
        {
          if(node.in1 != in1 && node.in2 == in2)
          {
            node.L.addAll(prevNode1.L);
            common.addAll(prevNode1.L);
            common.retainAll(prevNode2.L);
            node.L.removeAll(common);
          }
          else if(node.in1 == in1 && node.in2 != in2)
          {
            node.L.addAll(prevNode2.L);
            common.addAll(prevNode2.L);
            common.retainAll(prevNode1.L);
            node.L.removeAll(common);// set A - set B
          }
          else if(node.in1 != in1 && node.in2 != in2)
          {
            common.addAll(prevNode1.L); // union
            common.retainAll(prevNode2.L);// intersection
            node.L.addAll(common);
          }
          common.clear();
        }
      }
    }
    node.L.add(node.outLine+"/"+Integer.toString(1-node.out));
  }

  public static void main(String[] args)
  { 
    Set<String> globalFaultList = new HashSet<String>();
    
    Scanner scanner = null;
    try{
      System.out.println("Enter the file name:");
      Scanner filepath = new Scanner(System.in);
      String filename = filepath.nextLine();
      scanner = new Scanner(new File(filename));
    } catch(FileNotFoundException e){
      e.printStackTrace();
      return;
    }

    int no_inputs = 0, no_gates = 0;
    ArrayList<Node> nodes = new ArrayList<Node>();
    int tot_lines = 0, tot_nodes = 0;
    int counter = 0;

    while(scanner.hasNextLine())
    {
      String buffer = scanner.nextLine();
      if(buffer.isEmpty())  continue;

      if(counter == 2)
      {
        //creating input nodes
        for(int i=0; i<no_inputs; i++)
        {
          Node n = new Node(i, 0);
          nodes.add(n);
        }
      }

      Scanner s = new Scanner(buffer);
      String str = s.next();
      if(str.equalsIgnoreCase("INPUTS"))
      {
        //no. of input nodes
        no_inputs = s.nextInt();
      }
      else if(str.equalsIgnoreCase("GATES"))
      {
        //no. of gates
        no_gates = s.nextInt();
      }
      else if(!(str.isEmpty()))
      {
        tot_lines = no_inputs + no_gates;
        tot_nodes = tot_lines;
        
        int gate_no = Integer.parseInt(str.substring(1));
        int label = gate_no+no_inputs-1;
        String next_str = s.next();
        String gate_type = "";

        int i = 0;
        for(; i<next_str.length(); i++)
        {
          char ch = next_str.charAt(i);
          if(ch == '(')
          {
            break;
          }
          else
          {
            gate_type += ch;
          }
        }
        //creating gate node
        Node n = new Node(label, 0, gate_type);
        nodes.add(n);

        for(; i<next_str.length(); i++)
        {
          char ch = next_str.charAt(i);
          if(ch != '(' && ch != ',' && ch != ')')
          {
            int line_no = (int)(ch-'a');
            nodes.get(line_no).nextNode = nodes.get(label);
            nodes.get(label).prevNodes.add(nodes.get(line_no));
          }
        }
      }
      counter++;
    }

    Main circuit = new Main();
    circuit.levelizeCircuit(nodes);

    //test vectors
    Scanner sc = new Scanner(System.in);
    System.out.println("Enter no. of test vectors:");
    int no_testVectors = sc.nextInt();

    ArrayList<String> T = new ArrayList<String>();
    System.out.println("Enter the list of test vectors:");
    for(int i=0; i<no_testVectors; i++)
    {
      T.add(sc.next().trim());
    }

    //Iterating through test vectors
    String prevT = "", currT = "";
    Queue<Node> Q = new LinkedList<Node>();
    Set<Node> set = new HashSet<Node>();
  
    for(int i=0; i<no_testVectors; i++)
    { 
      System.out.println("Test vector "+(i+1)+" = "+T.get(i));

      currT = T.get(i);
      if(i==0)
      {
        for(char ch : T.get(i).toCharArray())
        {
          if(ch == '0') ch = '1';
          else ch = '0';
          prevT += Character.toString(ch);
        }
      }
      else
      {
        prevT = T.get(i-1);
      }

      for(int k=0; k<currT.length(); k++)
      {
        Node node = nodes.get(k);
        if(currT.charAt(k) != prevT.charAt(k))
        {
          if(node.nextNode.level == 1 && set.add(node.nextNode))
          {
            Q.add(node.nextNode);
          }
          int in = Character.getNumericValue(currT.charAt(k));
          node.setOutput(in);
          node.L.clear();
          node.L.add(node.outLine+"/"+Integer.toString(1-node.out));

          System.out.print("L"+node.outLine+" = ");
          node.displayFaultList();
        }
      }

      while(!Q.isEmpty())
      {
        Node node = Q.remove();
        if(node != null)
        {
          int out;
          if(node.gate_type.equalsIgnoreCase("NOT"))
          {
            Node prevNode = node.prevNodes.get(0);
            node.setInputs(prevNode.out);
            out = node.output(prevNode.out);
          }
          else
          {
            Node prevNode1 = node.prevNodes.get(0);
            Node prevNode2 = node.prevNodes.get(1);
            node.setInputs(prevNode1.out, prevNode2.out);
            out = node.output(prevNode1.out, prevNode2.out);
          }
          node.setOutput(out);
          circuit.setFaultList(node);

          if((node.label == tot_nodes-1 || node.nextNode.level == (node.level+1)) && set.add(node.nextNode))
          {
            Q.add(node.nextNode);
          }
          
          System.out.print("L"+node.outLine+" = ");
          node.displayFaultList();
        }
      }
      globalFaultList.addAll(nodes.get(tot_nodes-1).L);

      System.out.println();
      set.clear();
      Q.clear();
    }
    System.out.println("Final Fault List:");
    System.out.println("L = "+globalFaultList);
    float percentageFault = (float)globalFaultList.size()/(2*tot_lines)*100.0f;
    System.out.printf("Fault detected = %.2f %c",percentageFault, '%');
    System.out.println();
  }
}