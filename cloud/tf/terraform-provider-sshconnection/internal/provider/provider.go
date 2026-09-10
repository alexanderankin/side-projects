package provider

import (
	"context"
	_ "embed"

	"github.com/hashicorp/terraform-plugin-framework/datasource"
	"github.com/hashicorp/terraform-plugin-framework/ephemeral"
	"github.com/hashicorp/terraform-plugin-framework/provider"
	"github.com/hashicorp/terraform-plugin-framework/provider/schema"
	"github.com/hashicorp/terraform-plugin-framework/resource"
)

var _ provider.Provider = (*sshConnectionProvider)(nil)
var _ provider.ProviderWithEphemeralResources = (*sshConnectionProvider)(nil)

type sshConnectionProvider struct{ version string }

func New(version string) func() provider.Provider {
	return func() provider.Provider { return &sshConnectionProvider{version: version} }
}

func (p *sshConnectionProvider) Metadata(_ context.Context, _ provider.MetadataRequest, resp *provider.MetadataResponse) {
	//goland:noinspection SpellCheckingInspection
	resp.TypeName = "sshconnection"
	resp.Version = p.version
}

//go:embed providerDoc.md
var providerDoc string

func (p *sshConnectionProvider) Schema(_ context.Context, _ provider.SchemaRequest, resp *provider.SchemaResponse) {
	resp.Schema = schema.Schema{MarkdownDescription: providerDoc}
}

func (p *sshConnectionProvider) Configure(context.Context, provider.ConfigureRequest, *provider.ConfigureResponse) {
}

func (p *sshConnectionProvider) DataSources(context.Context) []func() datasource.DataSource {
	return nil
}
func (p *sshConnectionProvider) Resources(context.Context) []func() resource.Resource { return nil }

func (p *sshConnectionProvider) EphemeralResources(context.Context) []func() ephemeral.EphemeralResource {
	return []func() ephemeral.EphemeralResource{NewSSHConnection}
}
