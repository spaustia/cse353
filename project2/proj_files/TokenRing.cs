//This template is used to help you to build
//the code structure when you have no idea about
//how to start your work. You can ignore it if
//it's useless to you.

// Template for C# Project2 implementation.
// You may need extend this code for the
// monitor or priority extra credits, but this
// template should be a good starting point

// Your code may contain any part, including all,
// of this, but it must be COMMENTED much better!

using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading;

static class Address
{
    private static IPAddress ip = new IPAddress(new byte[] {127, 0, 0, 1});
    public static IPAddress IP { get { return ip; } }
}

class TokenRing
{
    static void Main(string[] argv)
    {
        // Add another argument if you're doing the
        // priority extra credit
        if (argv.Length != 1)
        {
            Console.WriteLine("Usage: TokenRing <# of nodes>");
        }

        byte numNodes = 0;
        try
        {
            numNodes = Convert.ToByte(argv[0]);
            if (numNodes > 254)
            {
                throw new Exception();
            }
        }
        catch (Exception)
        {
            Console.WriteLine("Cannot convert argument 1 to byte <# of nodes>");
        }

        Node[] nodes = new Node[numNodes];

        // You decide what ports to use!
        byte port;
        for (byte i = 0; i < numNodes; i++)
        {
            Socket sock = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.IP);

            // Make sure to get a good port for each node
            try
            {
                sock.Bind(new IPEndPoint(Address.IP, port));
            }
            catch (Exception)
            {
                // Socket already bound to port
            }

            nodes[i] = new Node(i, sock, port);
        }

        Thread[] threads = new Thread[numNodes];
        for (byte i=0; i<numNodes; i++)
        {
            threads[i] = new Thread(new ThreadStart(nodes[i].Run));
            try
            {
                threads[i].Start();
            }
            catch (Exception)
            {
                Console.WriteLine("Cannot start thread for node " + i + ".");
                Environment.Exit(1);
            }
        }

        for (byte i = 0; i < numNodes; i++)
        {
            try
            {
                // Connect the node to the next node in the ring.

                // Hint: This is a great place to print a debug message.
            }
            catch (Exception)
            {
                Console.WriteLine("Cannot connect node " + i + " to node " + (i + 1) + ".");
            }
        }

        // Main cannot exit until all threads are done.
        // Do thread synchronization here to make that happen
        // Describe the algorithm you chose in your README
    }
}

class Frame
{
    public byte AC, FC, DA, SA, size, FS;
    public byte[] data;

    public Frame(byte AC, byte DA, byte SA, byte[] data)
    {
        // Initialize
    }

    public static Frame MakeFrame(byte[] rep)
    {
        // Process binary representation into
        // frame object (or token object if it's
        // a token.
    }

    public byte[] ToBinary()
    {
        // Hint:
        MemoryStream ms = new MemoryStream(260);

        // use ms.WriteByte and ms.Write to put the
        // data into binary format.

        return ms.ToArray();
    }
}

class Token : Frame
{
    public Token(byte AC, byte DA, byte SA)
        : base(AC, DA, SA, new byte[0])
    {
        this.FC = 1;
    }
}

class Node
{
    // Choose a reasonable Token Holding Time
    // and describe your choice in your README
    protected int THT;

    public Node(byte num, Socket sock, int port)
    {
        // Assign object variables
    }

    public void Connect(Node target)
    {
        // Connect sockets
    }

    public void Run()
    {
        // Accept incoming connection and wait for
        // right neighbor to be ready.

        if (this.num == 0)
        {
            // This is the monitor. Make the first token.
        }

        try
        {
            while (true)
            {
                Frame frame = Frame.MakeFrame(Receive());

                if (frame is Token)
                {
                    Transmit(frame);
                }
                // else if it's for me, process it.
                // Write it to the node's output-file-i
            }
        }
        finally
        {
            // Close all open resources (i.e. sockets!)
        }
    }

    protected byte[] Receive()
    {
        // Read a complete frame off the wire
        // Be SURE that you loop until you get it all
        // by looking at the frame length field
    }

    protected void Transmit(Frame token)
    {
        // Send until THT reached
    }
}

class Monitor : Node
{
    // Extra credit. Describe everything you do in your
    // README
}