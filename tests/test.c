int * io_base= (int *) 0x10000000;
int a=20;

void func(int * i, int b)
{
  *io_base=b;
}

int main()
{
  func(io_base, a);
}

