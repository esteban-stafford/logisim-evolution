void * CONTROL= (void *) 0xfffffff1;
void * DATA= (void *) 0xfffffff2;
void * STATUS= (void *) 0xfffffff3;
void * OUT= (void *) 0xfffffff4;
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
  write_io(DATA, 1);
  write_io(CONTROL, 1);
  while(read_io(STATUS)!=2)
  {

  }
  int random=read_io(DATA);
  write_io(OUT, random);
}

__attribute__((section(".text.interrupt")))
__attribute__((interrupt))
void interrupt_routine(){}
