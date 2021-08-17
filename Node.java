import java.util.*;
public class Node
{
  int label;
  int level;
  String gate_type;
  ArrayList<Node> prevNodes;
  Node nextNode;
  String outLine;
  Set<String> L; //fault list for each outLine
  int in1, in2, out;
  
  public Node(int label, int level)
  {
    this.label = label;
    this.level = level;
    this.gate_type = "";
    this.outLine = Character.toString((char)('a'+label));
    this.L = new HashSet<String>();
  }

  public Node(int label, int level, String gate_type)
  {
    this.label = label;
    this.level = level;
    this.gate_type = gate_type;
    this.prevNodes = new ArrayList<Node>();
    this.outLine = Character.toString((char)('a'+label));
    this.L = new HashSet<String>();
  }
  
  public void setInputs(int in) //NOT
  {
    this.in1 = in;
    this.in2 = -1;
  }

  public void setInputs(int in1, int in2)
  {
    this.in1 = in1;
    this.in2 = in2;
  }

  public void setOutput(int out)
  {
    this.out = out;
  }

  public int output(int in) //NOT
  {
    int out = (in==0)?1:0;
    return out;
  }
  
  public int output(int in1, int in2)
  {
    int out = -1;
    in1 = (in1==0)?0:1;
    in2 = (in2==0)?0:1;
    
    if(gate_type.equalsIgnoreCase("AND"))
      out = in1&in2; //AND
    else if(gate_type.equalsIgnoreCase("OR")) 
      out = in1|in2; //OR
    else if(gate_type.equalsIgnoreCase("NAND")) 
      out = ((in1&in2) == 0)?1:0; //NAND
    else if(gate_type.equalsIgnoreCase("NOR")) 
      out = ((in1|in2) == 0)?1:0; //NOR
    else if(gate_type.equalsIgnoreCase("XOR")) 
      out = in1^in2; //XOR
    else if(gate_type.equalsIgnoreCase("XNOR")) 
      out = (in1 == in2)?1:0; //XNOR
    
    return out;
  }

  public boolean isGate()
  {
    return !(gate_type.isEmpty());
  }
  
  public void displayFaultList()
  {
    System.out.println(L);
  }
}
