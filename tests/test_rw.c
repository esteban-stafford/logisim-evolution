void * io_base= (void *) 0xfffffff0;
int a=1;

void write_io(int * addr, int v)
{
  *addr=v;
}

int read_io(int * addr)
{
    return addr[0];
}

int main()
{
  int v=read_io(io_base+1);
  write_io(io_base, v);
}

