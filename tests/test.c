int * io_base= (int *) 0xfffffff0;
int a=1;

void write_io(int * addr, int v)
{
  *addr=v;
}

int main()
{
  write_io(io_base, a);
}

